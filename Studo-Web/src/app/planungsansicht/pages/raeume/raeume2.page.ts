import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton
} from '@ionic/angular';
import { RaumService, RaumplanEintrag } from '../../services/raum.service';
import { VertretungsplanService } from '../../services/vertretungsplan.service';

// normal = regulärer Unterricht, vertretung = geändert, ausfall = Freistunde
type Stundenstatus = 'normal' | 'vertretung' | 'ausfall';

// Ein Belegungseintrag im Stundenplan eines Raums (UI-Modell dieser Komponente)
interface Belegungseintrag {
  stunde: number;
  wochentag: string;
  bezeichnung: string;
  person: string;
  klasse: string;
  status: Stundenstatus;
}

@Component({
  selector: 'app-raeume2',
  templateUrl: './raeume2.html',
  styleUrls: ['./raeume2.css'],
  standalone: true,
  imports: [
    CommonModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButtons,
    IonBackButton
  ]
})
export class Raeume2Page implements OnInit, OnDestroy {

  // ID aus der URL als Signal, damit "raum" automatisch neu berechnet wird,
  // sobald die Raumliste geladen ist (z. B. nach Neuladen der Seite)
  private raumId = signal<number | null>(null);
  raum = computed(() =>
    this.raumService.alleRaeume().find(r => r.id === this.raumId())
  );

  wochentage: string[] = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag'];

  // Immer alle festgelegten Stunden anzeigen. Unabhängig davon, ob der Raum belegt ist
  stunden = computed(() =>
    this.vertretungsplanService.alleZeitslots()
      .map(z => z.stundennummer)
      .sort((a, b) => a - b)
  );

  // Uhrzeit je Stunde, z. B. 1 -> "08:00–08:45"
  zeiten = computed(() => new Map(
    this.vertretungsplanService.alleZeitslots().map(z =>
      [z.stundennummer, `${z.start.slice(0, 5)}–${z.ende.slice(0, 5)}`] as [number, string]
    )
  ));

  belegung = signal<Belegungseintrag[]>([]);
  geladen = signal(false);

  private paramSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private raumService: RaumService,
    private vertretungsplanService: VertretungsplanService
  ) {}

  ngOnInit() {
    // Falls die Seite direkt aufgerufen wurde, ist die Raumliste noch leer
    if (this.raumService.alleRaeume().length === 0) {
      this.raumService.ladeRaeume();
    }

    // Stundenraster (Zeitslots) vom Backend laden
    this.vertretungsplanService.zeitslotsLaden();

    this.paramSub = this.route.paramMap.subscribe(params => {
      const raumId = Number(params.get('detailansicht'));
      this.raumId.set(raumId || null);
      this.ladeBelegung(raumId);
    });
  }

  ngOnDestroy() {
    this.paramSub?.unsubscribe();
  }

  // Sucht den Belegungseintrag für eine bestimmte Zelle (Stunde x Wochentag)
  getEintrag(stunde: number, wochentag: string): Belegungseintrag | undefined {
    return this.belegung().find(e => e.stunde === stunde && e.wochentag === wochentag);
  }

  private ladeBelegung(raumId: number) {
    this.belegung.set([]);
    this.geladen.set(false);

    if (!raumId) {
      return;
    }

    this.raumService.raumplanAbrufen(raumId).subscribe({
      next: (eintraege) => {
        this.belegung.set(eintraege.map(e => this.zuBelegungseintrag(e)));
        this.geladen.set(true);
      },
      error: (err) => {
        console.error('Fehler beim Laden des Raumplans', err);
        this.geladen.set(true);
      }
    });
  }

  private bestimmeStatus(e: RaumplanEintrag): Stundenstatus {
    if (e.freistunde) return 'ausfall';
    if (e.geaendert) return 'vertretung';
    return 'normal';
  }

  // Wandelt einen RaumplanEintrag in das UI-Modell dieser Seite um.
  // "wochentag" kommt als Enum-Name (z. B. "MONTAG") und wird auf die Anzeige-Strings gemappt.
  private zuBelegungseintrag(e: RaumplanEintrag): Belegungseintrag {
    const wochentagAnzeige = this.wochentage.find(
      w => w.toUpperCase() === e.wochentag.toUpperCase()
    ) ?? e.wochentag;

    return {
      stunde: e.stundennummer,
      wochentag: wochentagAnzeige,
      bezeichnung: e.fachName,
      person: e.lehrkraftKuerzel,
      klasse: e.lerngruppeKuerzel,
      status: this.bestimmeStatus(e)
    };
  }
}