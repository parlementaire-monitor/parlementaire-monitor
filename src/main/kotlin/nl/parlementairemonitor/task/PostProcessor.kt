package nl.parlementairemonitor.task

import jakarta.xml.bind.JAXBElement
import nl.parlementairemonitor.hook.FractieHook
import nl.parlementairemonitor.hook.MotieHook
import nl.parlementairemonitor.hook.PersoonHook
import nl.parlementairemonitor.hook.StemmingHook

class PostProcessor {

    companion object {
        private val hooks = listOf(
            FractieHook(),
            MotieHook(),
            PersoonHook(),
            StemmingHook(),
        )

        suspend fun process(content: JAXBElement<*>) {
            for (hook in hooks) {
                hook.filter(content)?.process()
            }
        }

    }

    interface Hook {
        suspend fun filter(content: JAXBElement<*>): Task?
    }

    interface Task {
        suspend fun process()
    }

}
