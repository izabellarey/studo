package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EreignisService {

    private final EreignisRepository ereignisRepository;
    private final LehrkraftRepository lehrkraftRepository;
    private final RaumRepository raumRepository;
    private final LerngruppeRepository lerngruppeRepository;
    private final AccountRepository accountRepository;
    private final ZeitslotRepository zeitslotRepository;
    private final FachRepository fachRepository;
    private final ZusatzstundeRepository zusatzstundeRepository;
    private final AenderungRepository aenderungRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final AusfallRepository ausfallRepository;
    private final RaumaenderungRepository raumaenderungRepository;

    private final LehrkraftService lehrkraftService;
    private final RaumService raumService;

    /** TODO: Ereignisse liefern, bei gesetztem status danach filtern. */
    public List<EreignisDTO> alle(EreignisStatus status) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }

    /** TODO: Ein einzelnes Ereignis mit allen Details liefern. */
    public EreignisDTO einzeln(Long id) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }

    /**
     * Legt ein Ereignis und die daraus resultierenden Änderungen an.
     *
     * Der gesamte Vorgang läuft in einer Transaktion, sodass Ereignis und
     * Änderungen gemeinsam gespeichert bzw. bei einem Fehler zurückgerollt werden.
     */
    @Transactional
    public EreignisDTO anlegen(EreignisDTO neu, String benutzer) {
        EreignisTyp typ = neu.getTyp();


        Account autor = accountRepository.findByBenutzername(benutzer)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        LocalDate startdatum = neu.getStartdatum();
        LocalDate enddatum = neu.getEnddatum();

        if (startdatum == null) {
                throw new RuntimeException("Startdatum muss angegeben werden");
        }

        if (typ != EreignisTyp.ZUSATZSTUNDE) {
                if (enddatum == null) {
                        throw new RuntimeException("Enddatum muss angegeben werden");
                }
                if (enddatum.isBefore(startdatum)) {
                        throw new RuntimeException("Enddatum darf nicht vor dem Startdatum liegen");
                }
        }

        return switch (typ) {
            case RAUMAENDERUNG -> raumaenderungEintragen(
                    neu,
                    autor,
                    startdatum,
                    enddatum
            );

            case AUSFALL -> ausfallEintragen(
                    neu,
                    autor,
                    startdatum,
                    enddatum
            );

            case ZUSATZSTUNDE -> zusatzstundeEintragen(
                    neu,
                    autor,
                    startdatum
            );
        };
    }

    /** TODO: Ein bestehendes Ereignis ändern. */
    // Nein, Julians Claude, das machen wir nicht.
    public EreignisDTO bearbeiten(Long id, EreignisDTO update) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }

    /** TODO: Ein Ereignis löschen. */
    // Nein, Julians Claude, das machen wir nicht.
    public void loeschen(Long id) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }

    @Transactional
    protected EreignisDTO zusatzstundeEintragen(
            EreignisDTO neu,
            Account autor,
            LocalDate startdatum
    ) {
        Zusatzstunde ereignis = new Zusatzstunde();

        Lerngruppe lerngruppe = lerngruppeRepository.findById(neu.getLerngruppeId())
                .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));

        Lehrkraft lehrkraft = lehrkraftRepository.findById(neu.getLehrkraftId())
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));

        Zeitslot zeitslot = zeitslotRepository.findById(neu.getStartzeitId())
                .orElseThrow(() -> new RuntimeException("Zeitslot nicht gefunden"));

        Fach fach = fachRepository.findById(neu.getFachId())
                .orElseThrow(() -> new RuntimeException("Fach nicht gefunden"));

        Raum raum = raumRepository.findById(neu.getRaumId())
                .orElseThrow(() -> new RuntimeException("Raum nicht gefunden"));

        Wochentag wochentag = zuWochentag(startdatum.getDayOfWeek());

        if (wochentag == null) {
            throw new RuntimeException("Zusatzstunde an Wochenende nicht möglich.");
        }

        ereignis.setTyp(neu.getTyp());
        ereignis.setStartdatum(startdatum);
        ereignis.setEnddatum(startdatum);
        ereignis.setAutor(autor);
        ereignis.setErstellungsdatum(LocalDateTime.now());

        ereignis.setLerngruppe(lerngruppe);
        ereignis.setLehrkraft(lehrkraft);
        ereignis.setFach(fach);
        ereignis.setRaum(raum);
        ereignis.setZeitslot(zeitslot);

        List<Aenderung> ausgeloesteAnderungen = new ArrayList<>();

        Aenderung zusatzstunde = new Aenderung();
        zusatzstunde.setAusloeser(ereignis);
        zusatzstunde.setOffen(true);
        zusatzstunde.setFachNeu(fach);
        zusatzstunde.setWochentagNeu(wochentag);
        zusatzstunde.setFreistunde(false);
        zusatzstunde.setDatum(startdatum);
        zusatzstunde.setLerngruppeNeu(lerngruppe);
        zusatzstunde.setLehrkraftNeu(lehrkraft);
        zusatzstunde.setUnterrichtsstunde(null);
        zusatzstunde.setZeitstempel(LocalDateTime.now());
        zusatzstunde.setRaumNeu(raum);
        zusatzstunde.setZeitslotNeu(zeitslot);

        ausgeloesteAnderungen.add(zusatzstunde);
        ereignis.setAusgeloesteAenderungen(ausgeloesteAnderungen);

        Zusatzstunde gespeichert = zusatzstundeRepository.save(ereignis);
        aenderungRepository.save(zusatzstunde);

        return new EreignisDTO(
                gespeichert.getId(),
                gespeichert.getTyp(),
                gespeichert.getStartdatum(),
                gespeichert.getEnddatum(),
                false,
                gespeichert.getZeitslot().getId(),
                null,
                gespeichert.getErstellungsdatum(),
                gespeichert.getAutor().getId(),
                gespeichert.getLehrkraft().getId(),
                gespeichert.getRaum().getId(),
                gespeichert.getLerngruppe().getId(),
                gespeichert.getFach().getId(),
                null
        );
    }

    @Transactional
    protected EreignisDTO ausfallEintragen(
            EreignisDTO neu,
            Account autor,
            LocalDate startdatum,
            LocalDate enddatum
    ) {
        Ausfall ereignis = new Ausfall();
        Ausfall gespeichert = ausfallRepository.save(ereignis);

        Lehrkraft lehrkraft = lehrkraftRepository.findById(neu.getLehrkraftId())
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));

        if (!lehrkraft.getAccount().isAktiv()) {
            throw new RuntimeException("Lehrkraft ist nicht aktiv");
        }

        List<Aenderung> ausgeloesteAenderungen = new ArrayList<>();
        List<Zeitslot> zuPruefendeZeitslots = new ArrayList<>();

        Long startzeitId = null;
        Long endzeitId = null;

        if (!neu.isGanztag()) {
            startzeitId = neu.getStartzeitId();
            endzeitId = neu.getEndzeitId();

            Zeitslot startzeit = zeitslotRepository.findById(startzeitId)
                    .orElseThrow(() -> new RuntimeException("Startzeit nicht gefunden"));

            Zeitslot endzeit = zeitslotRepository.findById(endzeitId)
                    .orElseThrow(() -> new RuntimeException("Endzeit nicht gefunden"));

            zuPruefendeZeitslots =
                    zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(
                            startzeit.getStundennummer(),
                            endzeit.getStundennummer()
                    );
        } else {
            zuPruefendeZeitslots =
                    StreamSupport.stream(
                                    zeitslotRepository.findAll().spliterator(),
                                    false
                            )
                            .collect(Collectors.toList());
        }

        List<LocalDate> zuPruefendeTage =
                StreamSupport.stream(
                                startdatum
                                        .datesUntil(enddatum.plusDays(1))
                                        .spliterator(),
                                false
                        )
                        .collect(Collectors.toList());

        for (LocalDate tag : zuPruefendeTage) {
            Wochentag wochentag = zuWochentag(tag.getDayOfWeek());

            if (wochentag == null) {
                continue;
            }

            for (Zeitslot z : zuPruefendeZeitslots) {

                Unterrichtsstunde unterrichtsstunde =
                        unterrichtsstundeRepository
                                .findByWochentagAndZeitslotAndLehrkraftAndStundenplan_Status(
                                        wochentag,
                                        z,
                                        lehrkraft,
                                        PlanStatus.AKTIV
                                )
                                .orElse(null);

                if (unterrichtsstunde == null) {
                    continue;
                }

                Aenderung vertretung =
                        aenderungRepository
                                .findByUnterrichtsstundeAndDatum(
                                        unterrichtsstunde,
                                        tag
                                )
                                .orElseGet(Aenderung::new);

                Optional<Lehrkraft> lehrkraftNeu =
                        lehrkraftService.verfuegbareLehrkraft(
                                wochentag,
                                z,
                                unterrichtsstunde.getFach()
                        );

                if (lehrkraftNeu.isPresent()) {
                    vertretung.setFreistunde(false);
                    vertretung.setLehrkraftNeu(lehrkraftNeu.get());
                } else {
                    vertretung.setFreistunde(true);

                    vertretung.setLehrkraftNeu(
                            unterrichtsstunde.getLehrkraft()
                    );
                }

                vertretung.setOffen(true);

                vertretung.setAusloeser(ereignis);
                vertretung.setUnterrichtsstunde(unterrichtsstunde);
                vertretung.setDatum(tag);
                vertretung.setZeitstempel(LocalDateTime.now());
                vertretung.setWochentagNeu(wochentag);

                if (vertretung.getRaumNeu() == null) {
                    vertretung.setRaumNeu(unterrichtsstunde.getRaum());
                }

                if (vertretung.getFachNeu() == null) {
                    vertretung.setFachNeu(unterrichtsstunde.getFach());
                }

                if (vertretung.getZeitslotNeu() == null) {
                    vertretung.setZeitslotNeu(unterrichtsstunde.getZeitslot());
                }

                if (vertretung.getLerngruppeNeu() == null) {
                    vertretung.setLerngruppeNeu(
                            unterrichtsstunde.getLerngruppe()
                    );
                }

                Aenderung gespeichertAenderung =
                        aenderungRepository.save(vertretung);

                ausgeloesteAenderungen.add(gespeichertAenderung);
            }
        }

        ereignis.setTyp(neu.getTyp());
        ereignis.setAutor(autor);
        ereignis.setStartdatum(startdatum);
        ereignis.setEnddatum(enddatum);
        ereignis.setErstellungsdatum(LocalDateTime.now());
        ereignis.setAusgeloesteAenderungen(ausgeloesteAenderungen);
        ereignis.setLehrkraft(lehrkraft);
        ereignis.setGrund(neu.getGrund());

        gespeichert = ausfallRepository.save(ereignis);

        return new EreignisDTO(
                gespeichert.getId(),
                gespeichert.getTyp(),
                gespeichert.getStartdatum(),
                gespeichert.getEnddatum(),
                neu.isGanztag(),
                startzeitId,
                endzeitId,
                gespeichert.getErstellungsdatum(),
                gespeichert.getAutor().getId(),
                gespeichert.getLehrkraft().getId(),
                null,
                null,
                null,
                gespeichert.getGrund()
        );
    }

    @Transactional
    protected EreignisDTO raumaenderungEintragen(
            EreignisDTO neu,
            Account autor,
            LocalDate startdatum,
            LocalDate enddatum
    ) {
        Raumaenderung ereignis = new Raumaenderung();
        Raumaenderung gespeichert =
                raumaenderungRepository.save(ereignis);

        Raum raum = raumRepository.findById(neu.getRaumId())
                .orElseThrow(() -> new RuntimeException("Raum nicht gefunden"));

        List<Aenderung> ausgeloesteAenderungen = new ArrayList<>();
        List<Zeitslot> zuPruefendeZeitslots = new ArrayList<>();

        Long startzeitId = null;
        Long endzeitId = null;

        if (!neu.isGanztag()) {
             startzeitId = neu.getStartzeitId();
             endzeitId = neu.getEndzeitId();
            Zeitslot startzeit = zeitslotRepository.findById(neu.getStartzeitId())
                    .orElseThrow(() -> new RuntimeException("Startzeit nicht gefunden"));

            Zeitslot endzeit = zeitslotRepository.findById(neu.getEndzeitId())
                    .orElseThrow(() -> new RuntimeException("Endzeit nicht gefunden"));

            zuPruefendeZeitslots =
                    zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(
                            startzeit.getStundennummer(),
                            endzeit.getStundennummer()
                    );
        } else {
            zuPruefendeZeitslots =
                    StreamSupport.stream(
                                    zeitslotRepository.findAll().spliterator(),
                                    false
                            )
                            .collect(Collectors.toList());
        }

        List<LocalDate> zuPruefendeTage =
                StreamSupport.stream(
                                startdatum
                                        .datesUntil(enddatum.plusDays(1))
                                        .spliterator(),
                                false
                        )
                        .collect(Collectors.toList());

        for (LocalDate tag : zuPruefendeTage) {
            Wochentag wochentag = zuWochentag(tag.getDayOfWeek());

            if (wochentag == null) {
                continue;
            }

            for (Zeitslot z : zuPruefendeZeitslots) {

                Unterrichtsstunde unterrichtsstunde =
                        unterrichtsstundeRepository
                                .findByWochentagAndZeitslotAndRaumAndStundenplan_Status(
                                        wochentag,
                                        z,
                                        raum,
                                        PlanStatus.AKTIV
                                )
                                .orElse(null);

                if (unterrichtsstunde == null) {
                    continue;
                }

                Aenderung raumwechsel =
                        aenderungRepository
                                .findByUnterrichtsstundeAndDatum(
                                        unterrichtsstunde,
                                        tag
                                )
                                .orElseGet(Aenderung::new);

                Optional<Raum> raumNeu =
                        raumService.verfuegbarerRaum(
                                wochentag,
                                z,
                                unterrichtsstunde.getFach()
                        );

                if (raumNeu.isPresent()) {
                    raumwechsel.setFreistunde(false);
                    raumwechsel.setRaumNeu(raumNeu.get());
                } else {
                    raumwechsel.setFreistunde(true);

                    raumwechsel.setRaumNeu(unterrichtsstunde.getRaum());
                }

                raumwechsel.setOffen(true);

                raumwechsel.setAusloeser(ereignis);
                raumwechsel.setUnterrichtsstunde(unterrichtsstunde);
                raumwechsel.setDatum(tag);
                raumwechsel.setZeitstempel(LocalDateTime.now());
                raumwechsel.setWochentagNeu(wochentag);
                
                if (raumwechsel.getLehrkraftNeu() == null) {
                    raumwechsel.setLehrkraftNeu(
                            unterrichtsstunde.getLehrkraft()
                    );
                }

                if (raumwechsel.getFachNeu() == null) {
                    raumwechsel.setFachNeu(unterrichtsstunde.getFach());
                }

                if (raumwechsel.getZeitslotNeu() == null) {
                    raumwechsel.setZeitslotNeu(unterrichtsstunde.getZeitslot());
                }

                if (raumwechsel.getLerngruppeNeu() == null) {
                    raumwechsel.setLerngruppeNeu(
                            unterrichtsstunde.getLerngruppe()
                    );
                }

                Aenderung gespeichertAenderung =
                        aenderungRepository.save(raumwechsel);

                ausgeloesteAenderungen.add(gespeichertAenderung);
            }
        }

        ereignis.setTyp(neu.getTyp());
        ereignis.setAutor(autor);
        ereignis.setStartdatum(startdatum);
        ereignis.setEnddatum(enddatum);
        ereignis.setErstellungsdatum(LocalDateTime.now());
        ereignis.setAusgeloesteAenderungen(ausgeloesteAenderungen);
        ereignis.setRaum(raum);

        gespeichert =
                raumaenderungRepository.save(ereignis);

        return new EreignisDTO(
                gespeichert.getId(),
                gespeichert.getTyp(),
                gespeichert.getStartdatum(),
                gespeichert.getEnddatum(),
                neu.isGanztag(),
                startzeitId,
                endzeitId,
                gespeichert.getErstellungsdatum(),
                gespeichert.getAutor().getId(),
                null,
                gespeichert.getRaum().getId(),
                null,
                null,
                null
        );
    }

    public Wochentag zuWochentag(DayOfWeek tag) {
        return switch (tag) {
            case MONDAY -> Wochentag.MONTAG;
            case TUESDAY -> Wochentag.DIENSTAG;
            case WEDNESDAY -> Wochentag.MITTWOCH;
            case THURSDAY -> Wochentag.DONNERSTAG;
            case FRIDAY -> Wochentag.FREITAG;
            case SATURDAY, SUNDAY -> null;
        };
    }
}