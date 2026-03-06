import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

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
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.isAdmin = user?.role === 'ADMIN';
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