import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/**
 * Flow is a cold asynchronous streams of data. Flow is inspired by
 * Rx approach to asynchronous programming.
 */

/**
 * Coroutine Context provides ContinuationInterceptor and ExceptionHandler.
 * ContinuationInterceptor are Dispatchers
 */

fun main() = runBlocking {

    exampleOf("Sequence (blocks amin thread)")

    fun prequels() : Sequence<String> = sequence {
        for (movie in listOf(episodeI, episodeII, episodeIII)) {
            Thread.sleep(DELAY)     // a long-computation
            yield(movie)
        }
    }

    prequels().forEach { movie -> log(movie) }

    log("Something else to be done here.")

    exampleOf("Suspending function (asynchronous)")

    suspend fun originals(): List<String> {
        delay(DELAY)    // a long-computation
        return listOf(episodeIV, episodeV, episodeVI)
    }

    launch {
        originals().forEach { movie -> log(movie) }
    }

    log("Something else to be done here.")

    delay(2 * DELAY)
    exampleOf("Simple flow")

    fun sequels(): Flow<String> = flow {
        for (movie in listOf(episodeVII, episodeVIII, episodeIX)) {
            delay(DELAY) // a long-computation
            emit(movie)
        }
    }

    launch {
        sequels().collect { movie -> log(movie) }
    }

    launch {
        for (i in 1..3) {
            log("Not blocked $i")
            delay(DELAY)
        }
    }

    log("Something else to be done here.")

    delay(4 * DELAY)
    exampleOf("Cold stream")

    val sequelFilms = sequels()

    sequelFilms.collect { movie -> log(movie) }

    delay(3 * DELAY)
    exampleOf("Collecting again")

    sequelFilms.collect { movie -> log(movie) }

    fun starWarsSounds() = flow {
        for (sound in listOf(chewieSound, r2d2Sound, blasterSound, saberSound)) {
            delay(DELAY)
            log("Emitting sound")
            emit(sound)
        }
    }

    withTimeoutOrNull((3.1 * DELAY).toLong()) {
        starWarsSounds().collect { sound -> log(sound) }
    }

    log("Done emitting sounds")

    exampleOf("Filter operator")

    forceUsers.asFlow()
        .filter { forceUser -> forceUser is Jedi }
        .collect { forceUser -> log(forceUser.name) }

    exampleOf("Map operator")

    suspend fun bestowSithTitle(forceUser: ForceUser): String {
        delay(DELAY)
        return "Darth ${forceUser.name}"
    }

    val sith = forceUsers.asFlow()
        .filter { forceUser -> forceUser is Sith }
        .map { forceUser -> bestowSithTitle(forceUser) }

    sith.collect { sithName -> log(sithName) }

    exampleOf("Transform operator")

    forceUsers.asFlow()
        .transform {  forceUser ->
            if (forceUser is Sith) {
                emit("Turning ${forceUser.name} to the Dark Side")
                emit(bestowSithTitle(forceUser))
            }
        }
        .collect { log(it) }

    exampleOf("Size-limiting operators")

    sith.take(2).collect { log(it) }

    exampleOf("Terminal operators")

    val jediLineage = forceUsers.asFlow()
        .filter { it is Jedi }
        .map { it.name }
        .reduce { a, b -> "$a trained by $b" }


        log(jediLineage)

    exampleOf("Completions")

    fun turnToDarkSide(): Flow<ForceUser> = forceUsers.asFlow()
        .transform { forceUser ->
            if (forceUser is Jedi) {
                emit(Sith(forceUser.name))
            }
        }

    exampleOf("Imperative completion")

    try {
        turnToDarkSide().collect { sith ->
            log("${sith.name}, your journey to the dark side is now completed")
        }
    } finally {
        log("Everything is proceeding as I have foreseen")
    }

    exampleOf("Imperative completion")

    turnToDarkSide()
        .onCompletion { log("Everything is proceeding as I have foreseen") }
        .collect { sith ->
        log("${sith.name}, your journey to the dark side is now completed")
    }

    fun duelOfTheFates(): Flow<ForceUser> = flow {
        for (forceUser in forceUsers) {
            delay(DELAY)
            log("Battling ${forceUser.name}")
            emit(forceUser)
        }
    }.transform { forceUser ->
        if (forceUser is Sith) {
            forceUser.name = "Darth ${forceUser.name}"
        }
        emit(forceUser)
    }.flowOn(Dispatchers.Default)

    duelOfTheFates().collect { log("Battled ${it.name}") }

    var time = measureTimeMillis {
        jediTrainees()
            .collect { jedi ->
                delay(3 * DELAY) // Jedi master training
                log("Jedi ${jedi.name} is now a master")
            }
    }

    log("Total time: $time ms")

    exampleOf("Buffer")

    time = measureTimeMillis {
        jediTrainees()
            .buffer()
            .collect { jedi ->
                delay(3 * DELAY) // Jedi master training
                log("Jedi ${jedi.name} is now a master")
            }
    }

    log("Total time: $time ms")

    exampleOf("Conflate")

    time = measureTimeMillis {
        jediTrainees()
            .conflate()
            .collect { jedi ->
                delay(3 * DELAY) // Jedi master training
                log("Jedi ${jedi.name} is now a master")
            }
    }

    log("Total time: $time ms")

    exampleOf("CollectLatest")

    time = measureTimeMillis {
        jediTrainees()
            .collectLatest { jedi ->
                log("Jedi master training for ${jedi.name}")
                delay(3 * DELAY) // Jedi master training
                log("Jedi ${jedi.name} is now a master")
            }
    }

    log("Total time: $time ms")

    exampleOf("zip")

    var characters = characterNames.asFlow()
    var weapons = weaponNames.asFlow()

    characters.zip(weapons) { character, weapon -> "$character: $weapon" }
        .collect { log(it) }


    exampleOf("onEach and zip with delay")

     characters = characterNames.asFlow().onEach { delay(DELAY / 2) }
     weapons = weaponNames.asFlow().onEach { delay(DELAY) }
     var start = System.currentTimeMillis()
     characters.zip(weapons) { character, weapon -> "$character: $weapon" }
        .collect { characterToWeapon ->
            log("$characterToWeapon at ${System.currentTimeMillis() - start} ms")
        }


    exampleOf("Combine")

    characters = characterNames.asFlow().onEach { delay(DELAY / 2) }
    weapons = weaponNames.asFlow().onEach { delay(DELAY) }
    start = System.currentTimeMillis()
    characters.combine(weapons) { character, weapon -> "$character: $weapon" }
        .collect { characterToWeapon ->
            log("$characterToWeapon at ${System.currentTimeMillis() - start} ms")
        }

    exampleOf("flatMapConcat")

    fun suitUp(string: String): Flow<String> = flow {
        emit("$string gets dressed for battle")
        delay(DELAY)
        emit("$string dons their helmet")
    }

    characterNames.asFlow().map { suitUp(it) }
        .collect { println(it) }

    start = System.currentTimeMillis()

    characterNames.asFlow().onEach { delay(DELAY / 2) }
        .flatMapConcat { suitUp(it) }
        .collect { value ->
            log("$value at ${System.currentTimeMillis() - start} ms")
        }

    exampleOf("flatMapMerge")

    characterNames.asFlow().onEach { delay(DELAY / 2) }
        .flatMapMerge { suitUp(it) }
        .collect { value ->
            log("$value at ${System.currentTimeMillis() - start} ms")
        }

    exampleOf("flatMapLatest")

    characterNames.asFlow().onEach { delay(DELAY / 2) }
        .flatMapLatest { suitUp(it) }
        .collect { value ->
            log("$value at ${System.currentTimeMillis() - start} ms")
        }

    exampleOf("Catching exceptional condition")

    try {
        midichlorianTest().collect { testResult ->
            log("$testResult")
            check(testResult <= CHOSEN_ONE_THRESHOLD) { "Test Result: $testResult" }
        }
    } catch (e: Throwable) {
        log("Could be the chosen one! ::: $e")
    }

    exampleOf("Catching from intermediate operator")

    try {
        midichlorianTestString().collect { log(it) }
    } catch (e: Throwable) {
        log("Could be the chosen one! ::: $e")
    }

    exampleOf("Exception transparency")

//    midichlorianTest()
//        .catch { log("Exception caught: $it") }
//        .collect { testResult ->
//            check(testResult <= CHOSEN_ONE_THRESHOLD) { "Test Result: $testResult" }
//            log("$testResult")
//        }

    exampleOf("Catching declaratively")

    midichlorianTest()
        .onEach { testResult ->
            check(testResult <= CHOSEN_ONE_THRESHOLD) { "Test Result: $testResult" }
            log("$testResult") }
        .catch { log("Exception caught: $it") }
        .collect()


}


fun jediTrainees(): Flow<ForceUser> = forceUsers.asFlow()
    .transform { forceUser ->
        if (forceUser is Padawan) {
            delay(DELAY)   // Jedi Knight training
            emit(forceUser)
        }
    }

fun midichlorianTest(): Flow<Int> = flow {
    for (key in midichlorianCounts.keys) {
        log("Testing $key")
        delay(DELAY)
        emit(midichlorianCounts[key] ?: 0)
    }
}

fun midichlorianTestString(): Flow<String> =
    flow<Int> {
    for (key in midichlorianCounts.keys) {
        log("Testing $key")
        delay(DELAY)
        emit(midichlorianCounts[key] ?: 0)
    }
}.map { testResult ->
        check(testResult <= CHOSEN_ONE_THRESHOLD) { "Test Result: $testResult" }
        "$testResult"
    }


