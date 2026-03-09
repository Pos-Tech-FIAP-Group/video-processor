import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { NavigationEnd } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { filter, Subscription } from 'rxjs';
import { VideoDetailDialogComponent } from '../video-detail-dialog/video-detail-dialog.component';

@Component({
  selector: 'app-videos-layout',
  standalone: true,
  imports: [RouterOutlet, MatDialogModule],
  template: `<router-outlet></router-outlet>`,
})
export class VideosLayoutComponent implements OnInit, OnDestroy {
  private sub?: Subscription;
  private openedForId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    const openDialog = (id: string) => {
      const dialogRef = this.dialog.open(VideoDetailDialogComponent, {
        data: { videoId: id },
        width: '560px',
        disableClose: false,
      });
      this.openedForId = id;
      dialogRef.afterClosed().subscribe(() => {
        this.openedForId = null;
        this.router.navigate(['/videos']);
      });
    };
    const checkAndOpen = () => {
      let r: ActivatedRoute | null = this.route;
      while (r?.firstChild) r = r.firstChild;
      const id = r?.snapshot.paramMap.get('id') ?? null;
      if (id && id !== this.openedForId) openDialog(id);
    };
    checkAndOpen();
    this.sub = this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
    ).subscribe(() => checkAndOpen());
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
