import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { createOutline } from 'ionicons/icons';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  InfiniteScrollCustomEvent,
  IonButton,
  IonContent,
  IonInfiniteScroll,
  IonInfiniteScrollContent,
  IonSearchbar,
  IonSpinner,
  IonIcon
} from '@ionic/angular';
import {
  AbgeschlosseneAenderungDTO,
  AenderungsverlaufService,
  EreignisTyp,
  OffeneAenderungDTO,
} from '../../services/aenderungsverlauf.service';
 
//Finias Route
const VERTRETUNGSPLAN_ROUTE = '/planungsansicht/vertretungsplan';
 
//abgeschlossene Änderungen pro Scroll
const SCHRITT = 25;
 
type BadgeArt = 'ausfall' | 'vertretung' | 'raumaenderung' | 'zusatzstunde';
 
const ANLASS_TEXT: Record<EreignisTyp, string> = {
  AUSFALL: 'Ausfall',
  RAUMAENDERUNG: 'Raumänderung',
  ZUSATZSTUNDE: 'Zusatzstunde',
};
 
const ANLASS_BADGE: Record<EreignisTyp, BadgeArt> = {
  AUSFALL: 'ausfall',
  RAUMAENDERUNG: 'raumaenderung',
  ZUSATZSTUNDE: 'zusatzstunde',
};
 
interface OffeneAnsicht {
  quelle: OffeneAenderungDTO;
  badge: BadgeArt;
  badgeText: string;
  titel: string;
  beschreibung: string | null;
  zeit: string;
}
 
interface AbgeschlosseneAnsicht {
  id: number;
  badge: BadgeArt;
  badgeText: string;
  einheit: string;
  nachricht: string;
  anlass: string;
  uhrzeit: string;
}
 
interface TagesGruppe {
  schluessel: string;
  titel: string;
  eintraege: AbgeschlosseneAnsicht[];
}
 
@Component({
  selector: 'app-aenderungsverlauf',
  standalone: true,
  templateUrl: './aenderungsverlauf.html',
  styleUrls: ['./aenderungsverlauf.css'],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonContent,
    IonButton,
    IonSearchbar,
    IonSpinner,
    IonInfiniteScroll,
    IonInfiniteScrollContent,
    IonIcon
  ],
})
export class AenderungsverlaufPage implements OnDestroy {
  private readonly service = inject(AenderungsverlaufService);
  private readonly router = inject(Router);

  constructor() {
    addIcons({createOutline});
  }
 
  readonly anlassFilter: { typ: EreignisTyp; label: string;}[] = [
    { typ: 'AUSFALL', label: 'Ausfall'},
    { typ: 'RAUMAENDERUNG', label: 'Raumänderung'},
    { typ: 'ZUSATZSTUNDE', label: 'Zusatzstunde'},
  ];
 
  // --- Offene Änderungen ------------------------------------------------------
  
  readonly anzahlOffen = this.service.anzahlOffen;
  readonly offeneLaden = this.service.offeneLaden;
  readonly offeneFehler = this.service.offeneFehler;
 
  readonly offene = computed<OffeneAnsicht[]>(() =>
    this.service.offeneAenderungen().map((a) => {
      const titel = a.titel?.trim() ?? '';
      const beschreibung = a.beschreibung?.trim() ?? '';
      return {
        quelle: a,
        badge: ANLASS_BADGE[a.typ],
        badgeText: ANLASS_TEXT[a.typ],
        titel: titel || beschreibung || ANLASS_TEXT[a.typ],
        beschreibung: titel && beschreibung ? beschreibung : null,
        zeit: relativeZeit(a.erstelltAm),
      };
    }),
  );
 
  // --- Abgeschlossene Änderungen (komplette Liste, Filter im Frontend) ----------
 
  private readonly alle = signal<AbgeschlosseneAenderungDTO[]>([]);
  readonly abgLaden = signal(false);
  readonly abgFehler = signal(false);
 
  readonly suche = signal('');
  readonly typFilter = signal<EreignisTyp | null>(null);
  readonly filterAktiv = computed(() => this.suche().trim() !== '' || this.typFilter() !== null);
 
  private readonly sichtbarBis = signal(SCHRITT);
 
  //neueste zuerst
  readonly gefiltert = computed(() => {
    const typ = this.typFilter();
    const begriff = normalisiere(this.suche());
    return this.alle().filter((e) => {
      if (typ && e.typ !== typ) return false;
      if (!begriff) return true;
      return normalisiere(`${e.betroffeneEinheit} ${e.aenderung}`).includes(begriff);
    });
  });
 
  readonly weitereVorhanden = computed(() => this.sichtbarBis() < this.gefiltert().length);
  readonly gruppen = computed(() => gruppiereNachTag(this.gefiltert().slice(0, this.sichtbarBis())));
 
  private anfrage?: Subscription;
 
  //Ionic-Lifecycle: läuft  beim Zurückkommen vom Vertretungsplan
  ionViewWillEnter(): void {
    this.service.offeneAktualisieren();
    this.abgeschlosseneLaden();
  }
 
  ngOnDestroy(): void {
    this.anfrage?.unsubscribe();
  }
 
  // --- Aktionen ------------------------------------------------------------------
 
  bearbeiten(a: OffeneAenderungDTO): void {
    this.router.navigate([VERTRETUNGSPLAN_ROUTE, a.klassenstufeId, a.klasseId], {
      queryParams: {
        aenderungId: a.id,
        stundeId: a.unterrichtsstundeId ?? undefined,
      },
    });
  }
 
  offeneErneutLaden(): void {
    this.service.offeneAktualisieren();
  }
 
  sucheGeaendert(event: CustomEvent): void {
    this.suche.set((event.detail?.value as string | null | undefined) ?? '');
    this.sichtbarBis.set(SCHRITT);
  }
 
  typUmschalten(typ: EreignisTyp): void {
    this.typFilter.update((aktuell) => (aktuell === typ ? null : typ));
    this.sichtbarBis.set(SCHRITT);
  }
 
  filterZuruecksetzen(): void {
    this.suche.set('');
    this.typFilter.set(null);
    this.sichtbarBis.set(SCHRITT);
  }
 
  mehrAnzeigen(event: InfiniteScrollCustomEvent): void {
    this.sichtbarBis.update((n) => n + SCHRITT);
    event.target.complete();
  }
 
  abgeschlosseneLaden(): void {
    this.anfrage?.unsubscribe();
    this.abgLaden.set(true);
    this.abgFehler.set(false);
    this.anfrage = this.service.ladeAbgeschlossene().subscribe({
      next: (liste) => {
        this.alle.set(liste);
        this.abgLaden.set(false);
      },
      error: () => {
        this.abgFehler.set(true);
        this.abgLaden.set(false);
      },
    });
  }
}
 
//----------------- Hilfsfunktionen -----------------------------
 
const UHRZEIT = new Intl.DateTimeFormat('de-DE', { hour: '2-digit', minute: '2-digit' });
const DATUM_KURZ = new Intl.DateTimeFormat('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
const DATUM_LANG = new Intl.DateTimeFormat('de-DE', {
  weekday: 'long',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});
 
//normalisierung 
function normalisiere(text: string): string {
  return text
    .toLowerCase()
    .replace(/ä/g, 'ae')
    .replace(/ö/g, 'oe')
    .replace(/ü/g, 'ue')
    .replace(/ß/g, 'ss')
    .replace(/\s+/g, ' ')
    .trim();
}
 
function tagesSchluessel(d: Date): string {
  return `${d.getFullYear()}-${d.getMonth()}-${d.getDate()}`;
}
 
function tageZurueck(d: Date): number {
  const heute = new Date();
  const a = new Date(heute.getFullYear(), heute.getMonth(), heute.getDate()).getTime();
  const b = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  return Math.round((a - b) / 86_400_000);
}
 
//Backend liefert UTC ohne Zeitzone -> als UTC lesen für deutsche Zeit
function parseZeit(iso: string): Date {
  const [datum, zeit = '00:00:00'] = iso.split('T');
  const [j, m, t] = datum.split('-').map(Number);
  const [h, min, s] = zeit.split(':').map(Number);
  return new Date(Date.UTC(j, m - 1, t, h, min, Math.floor(s)));
}


//datum + zeitdarstellung
function relativeZeit(iso: string): string {
  const d = parseZeit(iso);
  if (isNaN(d.getTime())) return '';
  const diff = tageZurueck(d);
  const tag = diff === 0 ? 'Heute' : diff === 1 ? 'Gestern' : DATUM_KURZ.format(d);
  return `${tag}, ${UHRZEIT.format(d)} Uhr`;
}
 
function tagesTitel(d: Date): string {
  const diff = tageZurueck(d);
  if (diff === 0) return 'Heute';
  if (diff === 1) return 'Gestern';
  return DATUM_LANG.format(d);
}
 
//badge aus anzeigeTyp, sonst anlass
function badgeAus(anzeigeTyp: string | null, typ: EreignisTyp): { art: BadgeArt; text: string } {
  const norm = normalisiere(anzeigeTyp ?? '');
  if (norm.includes('vertretung')) return { art: 'vertretung', text: 'Vertretung' };
  if (norm.includes('ausfall')) return { art: 'ausfall', text: 'Ausfall' };
  if (norm.includes('raum')) return { art: 'raumaenderung', text: 'Raumänderung' };
  if (norm.includes('zusatz')) return { art: 'zusatzstunde', text: 'Zusatzstunde' };
  return { art: ANLASS_BADGE[typ], text: ANLASS_TEXT[typ] };
}
 
function gruppiereNachTag(liste: AbgeschlosseneAenderungDTO[]): TagesGruppe[] {
  const gruppen: TagesGruppe[] = [];
  let aktuelle: TagesGruppe | undefined;
 
  for (const e of liste) {
    const d = parseZeit(e.zeitstempel);
    const schluessel = tagesSchluessel(d);
    if (!aktuelle || aktuelle.schluessel !== schluessel) {
      aktuelle = { schluessel, titel: tagesTitel(d), eintraege: [] };
      gruppen.push(aktuelle);
    }
    const badge = badgeAus(e.anzeigeTyp, e.typ);
    aktuelle.eintraege.push({
      id: e.id,
      badge: badge.art,
      badgeText: badge.text,
      einheit: e.betroffeneEinheit,
      nachricht: e.aenderung,
      anlass: ANLASS_TEXT[e.typ],
      uhrzeit: `${UHRZEIT.format(d)} Uhr`,
    });
  }
  return gruppen;
}