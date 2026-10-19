import { Routes } from '@angular/router';
import { authGuard } from './security/guards/auth.guard';
import { roleGuard } from './security/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  {
    path: 'login',
      loadComponent: () => 
        import('././login/login.page').then(
          (m) => m.LoginPage),
  },

  //---------- Schüler -------------
  {
    path: 'schueleransicht',
    canActivate: [authGuard, roleGuard(['SCHUELER'])],
    loadComponent: () =>
      import('./schueleransicht/layout/layout.schueler').then(
        (m) => m.LayoutSchuelerPage
      ),
    children: [
      { path: '', redirectTo: 'stundenplan', pathMatch: 'full' },
      {
        path: 'stundenplan',
        loadComponent: () =>
          import('./schueleransicht/pages/stundenplan/stundenplan').then(
            (m) => m.SchueleransichtPage
          ),
      },
      {
        path: 'aenderungen',
        loadComponent: () =>
          import('./schueleransicht/pages/aenderungen/aenderungenschueler').then(
            (m) => m.AenderungenSchuelerPage
          ),
      },
    ],
  },
  //-------------- Planung ------------------
  {
    path: 'planungsansicht',
    canActivate: [authGuard, roleGuard(['PLANUNG'])],
    loadComponent: () =>
      import('./planungsansicht/layout/layout.page').then(
        (m) => m.LayoutPage
      ),
    children: [

      {
        path: 'dashboard',
        loadComponent: () =>
          import('./planungsansicht/pages/dashboard/dashboard.page').then(
            (m) => m.DashboardPage
          ),
      },

      {
        path: 'schulansicht',
        loadComponent: () =>
          import('./planungsansicht/pages/schulansicht/schulansicht.page').then(
            (m) => m.SchulansichtPage
          ),
      },

      {
        path: 'klassen',
        loadComponent: () =>
          import('./planungsansicht/pages/klassen/klassen.page').then(
            (m) => m.KlassenPage
          ),
      },

      {
        path: 'initialerstundenplan',
        loadComponent: () =>
          import('./planungsansicht/pages/initstundenplan/initstundenplan.page').then(
            (m) => m.InitstundenplanPage
          ),
      },

      {
        path: 'vertretungsplan/:klassenstufeId/:lerngruppeId',
        loadComponent: () =>
          import('./planungsansicht/pages/klassen/vertretungsplan/vertretungsplan.page').then(
            (m) => m.VertretungsplanPage
          ),
      },

      {
        path: 'raeume',
        loadComponent: () =>
          import('./planungsansicht/pages/raeume/raeume.page').then(
            (m) => m.RaeumePage
          ),
      },

      {
        path: 'lehrer',
        loadComponent: () =>
          import('./planungsansicht/pages/lehrer/lehrer.page').then(
            (m) => m.LehrerPage
          ),
      },

      {
        path: 'schueler',
        loadComponent: () =>
          import('./planungsansicht/pages/schueler/schueler.page').then(
            (m) => m.SchuelerPage
          ),
      },

      {
        path: 'admins',
        loadComponent: () =>
          import('./planungsansicht/pages/admins/admins').then(
            (m) => m.AdminsPage
          ),
      },

      {
        path: 'aenderungsverlauf',
        loadComponent: () =>
          import('./planungsansicht/pages/aenderungsverlauf/aenderungsverlauf.page').then(
            (m) => m.AenderungsverlaufPage
          ),
      },

      {
        path: 'ereignisverlauf',
        loadComponent: () => import('./planungsansicht/pages/ereignisverlauf/ereignisverlauf').then(
          (m) => m.EreignisverlaufPage
        )
      },

      {
        path: 'stammdaten',
        loadComponent: () =>
          import('./planungsansicht/pages/stammdaten/stammdaten.page').then(
            (m) => m.StammdatenPage
          ),
      },

      {
        path: 'stammdaten2',
        loadComponent: () =>
          import('./planungsansicht/pages/stammdaten/stammdaten2').then(
            (m) => m.Stammdaten2Page
          ),
      },

      {
        path: 'konto',
        loadComponent: () =>
          import('./planungsansicht/pages/konto/konto.page').then(
            (m) => m.KontoPage
          ),
      },

     {
        path: 'klassen/:detailansicht',
        loadComponent: () =>
          import('./planungsansicht/pages/klassen/klassen2.page').then(
            (m) => m.Klassen2Page
          ),
      },

      {
        path: 'initstundenplan',
        loadComponent: () => 
          import('./planungsansicht/pages/initstundenplan/initstundenplan.page').then(
            (m) => m.InitstundenplanPage
          ),
      },

      {
        path: 'raeume/:detailansicht',
        loadComponent: () =>
          import('./planungsansicht/pages/raeume/raeume2.page').then(
            (m) => m.Raeume2Page
          ),
      },
    ],
  },


  //---------- Lehrkraft ------------
  {
    path: 'lehreransicht',
    canActivate: [authGuard, roleGuard(['LEHRKRAFT'])],
    loadComponent: () => 
      import('./lehreransicht/layout/layout.lehrer').then(
        (m) => m.LayoutLehrerPage
      ),
      children: [
      { path: '', redirectTo: 'stundenplan', pathMatch: 'full' },
      {
        path: 'stundenplan',
        title: 'Stundenplan',
        loadComponent: () =>
          import('./lehreransicht/pages/stundenplan/stundenplan').then(
            (m) => m.StundenplanPage
          ),
      },
      {
        path: 'aenderungen',
        title: 'Änderungen',
        loadComponent: () =>
          import('./lehreransicht/pages/aenderungen/aenderungen').then(
            (m) => m.AenderungenPage
          ),
      },
      {
        path: 'raeume',
        title: 'Räume',
        loadComponent: () =>
          import('./lehreransicht/pages/raeume/raeume').then(
            (m) => m.RaeumePage
          ),
      },
    ],
  },
];