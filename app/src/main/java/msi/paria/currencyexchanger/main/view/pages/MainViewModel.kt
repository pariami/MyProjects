package msi.paria.currencyexchanger.main.view.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import msi.paria.currencyexchanger.main.state.CurrencyScreenState
import msi.paria.currencyexchanger.main.view.pages.contract.CurrencyScreenEffect
import msi.paria.currencyexchanger.main.view.pages.contract.CurrencyScreenEvent
import msi.paria.currencyexchanger.util.isValidDecimalInput
import msi.paria.currencyexchanger.util.persianToEnglishNumber
import msi.paria.domain.model.Balance
import msi.paria.domain.model.Currency
import msi.paria.domain.model.Resource
import msi.paria.domain.model.Transaction
import msi.paria.domain.usecase.InternalUseCse
import msi.paria.domain.usecase.UseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCase: UseCase,
    private val internalUseCase: InternalUseCse,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(CurrencyScreenState())
    val state: StateFlow<CurrencyScreenState> = _state.asStateFlow()

    private val _effectFlow = MutableSharedFlow<CurrencyScreenEffect>(replay = 1)
    val effectFlow = _effectFlow.asSharedFlow()

    init {
        initialize()
    }

    private fun initialize() {
        updateState { it.copy(currencyResponse = Resource.Loading()) }
        fetchInitialData()
    }

    fun onEvent(event: CurrencyScreenEvent) {
        when (event) {
            is CurrencyScreenEvent.OnFromCurrencySelected -> handleFromCurrencySelected(event.currency)
            is CurrencyScreenEvent.OnToCurrencySelected -> handleToCurrencySelected(event.currency)
            is CurrencyScreenEvent.OnAmountValueEntered -> handleAmountValueEntered(event.amount)
            is CurrencyScreenEvent.OnCurrencyRateUpdated -> handleCurrencyRateUpdated()
            is CurrencyScreenEvent.OnSubmitButtonClicked -> handleSubmitButtonClicked()
        }
    }

    private fun fetchInitialData() {
        viewModelScope.launch(dispatcher) {
            launch { startFetchingExchangeRates() }
            launch { getAllBalances() }
        }
    }

    private fun handleFromCurrencySelected(currency: String) {
        updateState { it.copy(fromCurrency = currency) }
        checkBalanceAndConvert()
    }

    private fun handleToCurrencySelected(currency: String) {
        updateState { it.copy(toCurrency = currency) }
    }

    private fun handleAmountValueEntered(amount: String) {
        if(amount.isValidDecimalInput()){
            val normalizedAmount = if (amount.isNotEmpty()) persianToEnglishNumber(amount) else "0.0"
            updateState { it.copy(amount = normalizedAmount) }
            checkBalanceAndConvert()
        }
    }

    private fun handleCurrencyRateUpdated() {
        checkBalanceAndConvert()
    }

    private fun handleSubmitButtonClicked() {
        fetchCommissionAndSubmit()
    }

    private suspend fun startFetchingExchangeRates() = flow {
        while (true) {
            emit(Unit)
            delay(5000)
        }
    }.collect {
        useCase.getExchangeRates(_state.value.fromCurrency).collect { resource ->
            updateState { it.copy(currencyResponse = resource) }
            handleExchangeRatesResponse(resource)
        }
    }

    private fun getAllBalances() {
        viewModelScope.launch(dispatcher) {
            useCase.getAllBalance().collect { balances ->
                if (balances.isEmpty()) {
                    useCase.insertBalance(Balance(1, "USD", 100.0))
                } else {
                    updateState { it.copy(balances = balances) }
                }
            }
        }
    }

    private fun checkBalanceAndConvert() {
        viewModelScope.launch(dispatcher) {
            val amount = _state.value.amount.toDoubleOrNull() ?: 0.0
            val canConvert = internalUseCase.checkBalance(_state.value.fromCurrency, amount)

            val message = if (!canConvert) "This Currency isn't enough for convert" else ""
            updateState { it.copy(canConvert = canConvert, message = message) }
            emitEffect(CurrencyScreenEffect.ChangeSubmitButtonState)

            if (canConvert) convertCurrency()

        }
    }

    private fun convertCurrency() {
        val amount = _state.value.amount
        val fromCurrency = _state.value.fromCurrency
        val toCurrency = _state.value.toCurrency
        val ratesResponse = _state.value.currencyResponse

        val convertedAmount =
            internalUseCase.currencyConversion(ratesResponse, amount, fromCurrency, toCurrency)
        updateState {
            it.copy(
                canConvert = _state.value.canConvert && (_state.value.fromCurrency != _state.value.toCurrency) ,
                convertedAmount = convertedAmount,
                message = "$amount $fromCurrency = $convertedAmount $toCurrency, commissionFee: ${it.commissionFee}"
            )
        }
    }

    private fun fetchCommissionAndSubmit() {
        viewModelScope.launch(dispatcher) {
            val commissionFee = internalUseCase.getCommissionUseCase()
            updateState { it.copy(commissionFee = commissionFee) }
            submitConversion()
        }
    }

    private fun submitConversion() {
        if (_state.value.canConvert) {
            viewModelScope.launch(dispatcher) {
                val fromCurrency = _state.value.fromCurrency
                val toCurrency = _state.value.toCurrency
                val amount = _state.value.amount.toDouble()
                val convertedAmount = _state.value.convertedAmount

                internalUseCase.transferFunds(fromCurrency, toCurrency, amount, convertedAmount)
                insertTransaction()
                getAllBalances()
            }
        }
        emitEffect(CurrencyScreenEffect.ShowResultDialog)
    }

    private fun transferFunds(
        fromCurrency: String,
        toCurrency: String,
        fromAmount: Double,
        convertedCurrency: Double
    ) {
        viewModelScope.launch(dispatcher) {
            internalUseCase.transferFunds(fromCurrency, toCurrency, fromAmount, convertedCurrency)
        }
    }

    private suspend fun insertTransaction() {
        val transaction = Transaction(
            amount = _state.value.amount.toDouble(),
            fromCurrency = _state.value.fromCurrency,
            toCurrency = _state.value.toCurrency,
            convertedAmount = _state.value.convertedAmount,
            commissionFee = _state.value.commissionFee
        )
        useCase.insertTransaction(transaction)
    }

    private fun handleExchangeRatesResponse(resource: Resource<Currency>) {
        when (resource) {
            is Resource.Error -> updateState {
                it.copy(
                    message = resource.message ?: "Unknown error"
                )
            }

            is Resource.Success -> {
                updateState { it.copy(rates = resource.data?.rates ?: emptyMap()) }
                emitEffect(CurrencyScreenEffect.OnRatesReceived)
            }

            else -> Unit
        }
    }

    private fun updateState(update: (CurrencyScreenState) -> CurrencyScreenState) {
        _state.update(update)
    }

    private fun emitEffect(effect: CurrencyScreenEffect) {
        viewModelScope.launch { _effectFlow.emit(effect) }
    }

}