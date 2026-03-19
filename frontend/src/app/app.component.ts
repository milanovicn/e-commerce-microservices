import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './services/auth.service';
import { TracingService } from './services/tracing.service'; // adjust path if needed

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  currentUser$ = this.authService.currentUser$;
  isAdmin = false;

  constructor(
    public authService: AuthService,
    private router: Router,
    private tracingService: TracingService,
  ) {}

  ngOnInit() {
    this.tracingService.init(); // initializes OTel once at app startup

    this.authService.currentUser$.subscribe(user => {
      this.isAdmin = user?.role === 'ADMIN';
    });

    // Track all route navigations in one place — no changes needed in any other component
    let previousUrl = '';
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.tracingService.trackTabSwitch(previousUrl, event.urlAfterRedirects);
      previousUrl = event.urlAfterRedirects;
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goToAdmin(): void {
    this.router.navigate(['/admin']);
  }
}