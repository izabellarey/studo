import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonContent, IonIcon, IonMenu, IonRouterOutlet, IonSplitPane, IonImg
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  calendarOutline, swapHorizontalOutline, logOutOutline
} from 'ionicons/icons';

@Component({
  selector: 'app-schueleransicht-layout',
  templateUrl: './layout.schueler.html',
  styleUrls: ['./layout.schueler.css'],
  standalone: true,
  imports: [IonContent, IonIcon, IonMenu, IonImg, IonRouterOutlet, IonSplitPane,
            RouterLink, RouterLinkActive],
})
export class LayoutSchuelerPage {
  constructor(private router: Router) {
    addIcons({ calendarOutline, swapHorizontalOutline, logOutOutline });
  }

  logout() {
    this.router.navigate(['/login']);
  }
}