package de.cau.sp.stundenplanung.solver;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Prüft nur, dass defineConstraints alle Regeln registriert. Ob eine Regel fachlich richtig
 * rechnet, lässt sich so nicht prüfen, dafür bräuchte es den ConstraintVerifier.
 */
class StundenplanConstraintProviderTest {

    private static final Set<String> ERWARTETE_REGELN = Set.of(
            /* hart */
            "Wechselnde Lehrkraft im selben Fach",
            "Raumwechsel innerhalb einer Doppelstunde",
            "Klassenleitung unterrichtet die eigene Klasse nicht",
            "Mehr als zwei Stunden eines Fachs am Tag",
            "Lehrkraft doppelt belegt",
            "Lerngruppe doppelt belegt",
            "Raum doppelt belegt",
            "Lehrkraft zu dieser Zeit gesperrt",
            "Raum zu dieser Zeit gesperrt",
            "Lehrkraft unterrichtet fachfremd",
            "Raumtyp passt nicht zum Fach",
            "Nicht im Klassenzimmer der Lerngruppe",
            "Raum zu klein",
            "Zu viele Stunden an einem Tag",
            /* mittel */
            "Deputat ueberschritten",
            /* weich */
            "Freistunde einer Lerngruppe",
            "Fachstunden am selben Tag nicht als Block",
            "Tag beginnt nicht in der ersten Stunde",
            "Ungleiche Tageslaengen",
            "Freistunde einer Lehrkraft",
            "Unterricht in einer Randstunde",
            "Hauptfach am Nachmittag",
            "Fach an aufeinanderfolgenden Tagen",
            "Unerwuenschte Zeit der Lehrkraft",
            "Bevorzugte Zeit der Lehrkraft getroffen",
            "Lehrkraft unterrichtet bevorzugte Klasse",
            "Raumwechsel zwischen Folgestunden");

    private final List<String> namen = new ArrayList<>();

    /* Jeder Aufruf der Stream-Kette liefert wieder einen Mock, asConstraint() merkt sich den Regelnamen. */
    private final Answer<Object> kette = new Answer<>() {
        @Override
        public Object answer(InvocationOnMock aufruf) throws Throwable {
            if (aufruf.getMethod().getName().equals("asConstraint")) {
                Object[] args = aufruf.getArguments();
                namen.add((String) args[args.length - 1]);
                return mock(Constraint.class);
            }
            Class<?> rueckgabe = aufruf.getMethod().getReturnType();
            if (rueckgabe.isInterface()) {
                return mock(rueckgabe, this);
            }
            return RETURNS_DEFAULTS.answer(aufruf);
        }
    };

    private Constraint[] regeln() {
        ConstraintFactory factory = mock(ConstraintFactory.class, kette);
        return new StundenplanConstraintProvider().defineConstraints(factory);
    }

    @Test
    void defineConstraintsLiefertAlle27Regeln() {
        Constraint[] regeln = regeln();

        assertEquals(27, regeln.length);
        assertTrue(Arrays.stream(regeln).allMatch(Objects::nonNull));
    }

    @Test
    void defineConstraintsVergibtJedenRegelnamenGenauEinmal() {
        regeln();

        assertEquals(namen.size(), new HashSet<>(namen).size());
    }

    @Test
    void defineConstraintsEnthaeltGenauDieErwartetenRegeln() {
        regeln();

        assertEquals(ERWARTETE_REGELN, new HashSet<>(namen));
    }
}
