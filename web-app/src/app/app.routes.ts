import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'videos', pathMatch: 'full' },
      { path: 'videos', loadComponent: () => import('./features/videos/video-list/video-list.component').then(m => m.VideoListComponent) },
      { path: 'videos/upload', loadComponent: () => import('./features/videos/video-upload/video-upload.component').then(m => m.VideoUploadComponent) },
      { path: 'videos/:id', loadComponent: () => import('./features/videos/video-detail/video-detail.component').then(m => m.VideoDetailComponent) },
    ],
  },
  { path: '**', redirectTo: '' },
];
