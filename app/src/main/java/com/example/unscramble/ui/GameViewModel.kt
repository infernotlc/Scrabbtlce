package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.MAX_SKIP
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {

    private val _uiState =
        MutableStateFlow(GameUiState()) //sonradan değiştirilebilir, durumu içeriden değiştirip
    val uiState: StateFlow<GameUiState> =
        _uiState.asStateFlow() //sonradan değişmez, dışarı değiştirelemez veriyoruz

    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()//benzersiz elemanları saklar
    private var usedSkips: MutableSet<Int> = mutableSetOf()
    var userGuess by mutableStateOf("")
        private set // sadece bu sınıf için değiştirebilir yapar dışarıdan sadece okunur

    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        updateUserGuess("")

    }

    private fun updateGameState(updatedScore: Int, ) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currenState ->
                currenState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true,
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }

     }

    private fun updateSkipState() {
            _uiState.update { currenState ->
                currenState.copy(
                    currentSkipCount = currenState.currentSkipCount.inc(),
                    )
            }
        }


    fun skipWord() {
        updateSkipState()
        updateUserGuess("")
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    init {
        resetGame()
    }
}
