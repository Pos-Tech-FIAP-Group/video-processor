import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'videos', pathMatch: 'full' },
      {
        path: 'videos',
        loadComponent: () => import('./features/videos/videos-layout/videos-layout.component').then(m => m.VideosLayoutComponent),
        children: [
          { path: '', loadComponent: () => import('./features/videos/video-list/video-list.component').then(m => m.VideoListComponent) },
          { path: 'upload', loadComponent: () => import('./features/videos/video-upload/video-upload.component').then(m => m.VideoUploadComponent) },
          { path: ':id', loadComponent: () => import('./features/videos/video-list/video-list.component').then(m => m.VideoListComponent) },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
