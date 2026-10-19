-- TESTDATEN für Entwicklung/Debugging.
-- Vor der finalen Abgabe entfernen oder durch echte Startdaten ersetzen.

INSERT INTO jahrgangsstufe (id, nummer, prio) VALUES (1, 7, 1);

INSERT INTO raum (id, bezeichnung, kapazitaet, art) VALUES (1, 'R102', 30, 'NORMAL');

INSERT INTO lerngruppe (id, bezeichnung, jahrgangsstufe_id, schueleranzahl, klassenzimmer_id, art)
VALUES (1, '7a', 1, 25, 1, 'KLASSE');

INSERT INTO fach (id, kuerzel, name, benoetigter_raumtyp) VALUES (1, 'MA', 'Mathematik', 'NORMAL');

INSERT INTO zeitslot (id, stundennummer, start, ende, pause) VALUES (1, 1, '08:00:00', '08:45:00', false);

-- Account + Lehrkraft
INSERT INTO account (id, name, vorname, benutzername, passwort, rolle, aktiv)
VALUES (1, 'Meyer', 'Petra', 'petra-meyer', 'test123', 'LEHRKRAFT', true);

INSERT INTO lehrkraft (id, account_id, kuerzel, anstellungsverhaeltnis, deputatsstunden)
VALUES (1, 1, 'MEY', 'VOLLZEIT', 25);

INSERT INTO stundenplan (id, schuljahr, halbjahr, startdatum, enddatum, status, generation_id, lerngruppe_id, bewertungs_score)
VALUES (1, '2026/2027', 1, '2026-09-01', '2027-01-31', 'ENTWURF', 1, 1, 85);

INSERT INTO stundenplan (id, schuljahr, halbjahr, startdatum, enddatum, status, generation_id, lerngruppe_id, bewertungs_score)
VALUES (2, '2026/2027', 1, '2026-09-01', '2027-01-31', 'ENTWURF', 1, 1, 70);

INSERT INTO unterrichtsstunde (id, wochentag, zeitslot_id, raum_id, lehrkraft_id, fach_id, lerngruppe_id, stundenplan_id)
VALUES (1, 'MONTAG', 1, 1, 1, 1, 1, 1);