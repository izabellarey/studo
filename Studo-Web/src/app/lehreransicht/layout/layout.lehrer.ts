import { Component, OnInit, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../security/services/auth.service';
import { LehreransichtService } from '../services/lehreransicht.service';
 
import {
  IonContent,
  IonIcon,
  IonMenu,
  IonRouterOutlet,
  IonSplitPane,
  IonImg
} from '@ionic/angular';
 
import {
  calendarOutline,
  syncOutline,
  businessOutline,
  logOutOutline,
  notificationsOutline
} from 'ionicons/icons';
 
import { addIcons } from 'ionicons';
 
@Component({
  selector: 'app-layout-lehrer',
  templateUrl: './layout.lehrer.html',
  styleUrls: ['./layout.lehrer.css'],
  imports: [
    IonContent,
    IonIcon,
    IonMenu,
    IonImg,
    IonRouterOutlet,
    IonSplitPane,
    RouterLink,
    RouterLinkActive
  ]
})
export class LayoutLehrerPage implements OnInit {
  protected authService = inject(AuthService);
  private lehrerService = inject(LehreransichtService);
 
  readonly anzahlOffen = this.lehrerService.anzahlOffen;
 
  //Initialen für den Avatar oben rechts, kommen direkt aus dem Login (kein extra Backend-Aufruf)
  readonly kuerzel = computed(() => {
    const user = this.authService.currentUser();
    return user ? (user.vorname.charAt(0) + user.name.charAt(0)).toUpperCase() : '';
  });
 
  readonly vollerName = computed(() => {
    const user = this.authService.currentUser();
    return user ? `${user.vorname} ${user.name}` : '';
  });
 
  constructor() {
    addIcons({
      calendarOutline,
      syncOutline,
      businessOutline,
      logOutOutline,
      notificationsOutline
    });
  }
 
  ngOnInit() {
    this.lehrerService.ladeAenderungen();
  }
}