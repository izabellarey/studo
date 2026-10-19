import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../security/services/auth.service';
import { AenderungsverlaufService } from '../services/aenderungsverlauf.service';

import {
  IonContent,
  IonIcon,
  IonMenu,
  IonRouterOutlet,
  IonSplitPane,
  IonImg,
  IonBreadcrumb,
  IonBreadcrumbs,
  IonFab,
  IonFabButton
} from '@ionic/angular';

import {
  gridOutline,
  personOutline,
  shieldOutline,
  serverOutline,
  bookOutline,
  settingsOutline,
  newspaperOutline,
  schoolOutline,
  briefcaseOutline,
  businessOutline,
  peopleOutline,
  syncOutline,
  logOutOutline,
  notificationsOutline,
  calendarOutline
} from 'ionicons/icons';

import { addIcons } from 'ionicons';
import { AccountService } from '../services/konto.service';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.page.html',
  styleUrls: ['./layout.page.css'],
  imports: [
    IonContent,
    IonIcon,
    IonMenu,
    IonImg,
    IonRouterOutlet,
    IonSplitPane,
    RouterLink,
    RouterLinkActive, 
    IonBreadcrumb,
    IonBreadcrumbs,
    IonFab,
    IonFabButton
  ]
})
export class LayoutPage implements OnInit {
  protected authService = inject(AuthService);
  accountService = inject(AccountService);
  
  private verlauf = inject(AenderungsverlaufService);
  readonly anzahlOffen = this.verlauf.anzahlOffen;


  constructor() {
    addIcons({
      gridOutline,
      businessOutline,
      personOutline,
      shieldOutline,
      syncOutline,
      serverOutline,
      bookOutline,
      settingsOutline,
      newspaperOutline,
      schoolOutline,
      briefcaseOutline,
      peopleOutline,
      logOutOutline,
      notificationsOutline,
      calendarOutline
    });
  }

  ngOnInit() {
    this.accountService.ladeProfil().subscribe({
      error: err => console.error('Profil konnte nicht geladen werden', err)
    });

    this.verlauf.startePolling();
  }

  ngOnDestroy() {
    this.verlauf.stoppePolling();
  }

}