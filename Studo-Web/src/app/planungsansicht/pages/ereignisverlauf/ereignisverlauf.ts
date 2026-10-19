import { Component, computed, inject } from '@angular/core';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonSpinner,
} from '@ionic/angular';
import { EreignisTyp } from '../../services/aenderungsverlauf.service';
import { EreignisverlaufService, EreignisDTO } from '../../services/ereignisverlauf.service';
 
type BadgeArt = 'ausfall' | 'raumaenderung' | 'zusatzstunde';
 
const TYP_TEXT: Record<EreignisTyp, string> = {
  AUSFALL: 'Ausfall',
  RAUMAENDERUNG: 'Raumänderung',
  ZUSATZSTUNDE: 'Zusatzstunde',
};
 
const TYP_BADGE: Record<EreignisTyp, BadgeArt> = {
  AUSFALL: 'ausfall',
  RAUMAENDERUNG: 'raumaenderung',
  ZUSATZSTUNDE: 'zusatzstunde',
};
 
interface EreignisZeile {
  id: number;
  badge: BadgeArt;
  typText: string;
  eingetragen: string;
  datum: string;
  stunden: string;
  betroffen: string;
  grund: string;
}
 
@Component({
  selector: 'app-ereignisverlauf',
  templateUrl: './ereignisverlauf.html',
  styleUrls: ['./ereignisverlauf.css'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonSpinner,
  ],
})
export class EreignisverlaufPage {
  public ereignisService = inject(EreignisverlaufService);
 
  //Ionic-Lifecycle: lädt bei jedem Öffnen neu (neue Ereignisse aus dem Dashboard)
  ionViewWillEnter() {
    this.ereignisService.ladeEreignisse();
    this.ereignisService.ladeNachschlagedaten();
  }
 
  laedt = this.ereignisService.laedt;
  hatFehler = this.ereignisService.hatFehler;
 
 
  //---------------- Anzeige: IDs in Namen umwandeln --------------------------------
 
 
  zeilen = computed<EreignisZeile[]>(() => {
    const lehrkraefte = new Map(this.ereignisService.alleLehrkraefte().map((l) => [l.id, `${l.vorname} ${l.nachname}`]));
    const raeume = new Map(this.ereignisService.alleRaeume().map((r) => [r.id, r.bezeichnung]));
    const klassen = new Map(this.ereignisService.alleLerngruppen().map((k) => [k.id, k.bezeichnung]));
    const faecher = new Map(this.ereignisService.alleFaecher().map((f) => [f.id, f.name]));
    const stunden = new Map(this.ereignisService.alleZeitslots().map((z) => [z.id, z.stundennummer]));
 
    return this.ereignisService.alleEreignisse().map((e: EreignisDTO) => ({
      id: e.id,
      badge: TYP_BADGE[e.typ],
      typText: TYP_TEXT[e.typ],
      eingetragen: formatZeitstempel(e.erstellungsdatum),
      datum: formatZeitraum(e.startdatum, e.enddatum),
      stunden: formatStunden(e, stunden),
      betroffen: formatBetroffen(e, lehrkraefte, raeume, klassen, faecher),
      grund: e.grund?.trim() || '–',
    }));
  });
 
  neuLaden() {
    this.ereignisService.ladeEreignisse();
  }
}
 
 
//---------------- Hilfsfunktionen --------------------------------
 
 
function formatDatum(iso: string | null): string {
  if (!iso) return '';
  const [jahr, monat, tag] = iso.substring(0, 10).split('-');
  return `${tag}.${monat}.${jahr}`;
}
 
function formatZeitraum(start: string, ende: string | null): string {
  if (!ende || ende === start) return formatDatum(start);
  return `${formatDatum(start)} – ${formatDatum(ende)}`;
}
 
function formatZeitstempel(iso: string): string {
  if (!iso) return '';
  return `${formatDatum(iso)}, ${iso.substring(11, 16)} Uhr`;
}
 
function formatStunden(e: EreignisDTO, stunden: Map<number, number>): string {
  if (e.ganztag) return 'ganztägig';
  const start = e.startzeitId != null ? (stunden.get(e.startzeitId) ?? `Slot ${e.startzeitId}`) : null;
  const ende = e.endzeitId != null ? (stunden.get(e.endzeitId) ?? `Slot ${e.endzeitId}`) : null;
  if (start == null) return '–';
  if (ende == null || ende === start) return `${start}. Stunde`;
  return `${start}.–${ende}. Stunde`;
}
 
function formatBetroffen(
  e: EreignisDTO,
  lehrkraefte: Map<number, string>,
  raeume: Map<number, string>,
  klassen: Map<number, string>,
  faecher: Map<number, string>,
): string {
  const teile: string[] = [];
  if (e.lehrkraftId != null) teile.push(lehrkraefte.get(e.lehrkraftId) ?? `Lehrkraft ${e.lehrkraftId}`);
  if (e.raumId != null) teile.push(raeume.get(e.raumId) ?? `Raum ${e.raumId}`);
  if (e.lerngruppeId != null) teile.push(`Klasse ${klassen.get(e.lerngruppeId) ?? e.lerngruppeId}`);
  if (e.fachId != null) teile.push(faecher.get(e.fachId) ?? `Fach ${e.fachId}`);
  return teile.length ? teile.join(' · ') : '–';
}