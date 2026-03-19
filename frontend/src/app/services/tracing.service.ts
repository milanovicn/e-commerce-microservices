import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { metrics, MeterProvider } from '@opentelemetry/api';
import { OTLPMetricExporter } from '@opentelemetry/exporter-metrics-otlp-http';
import { MeterProvider as SdkMeterProvider, PeriodicExportingMetricReader } from '@opentelemetry/sdk-metrics';
import { Resource } from '@opentelemetry/resources';
import { SEMRESATTRS_SERVICE_NAME } from '@opentelemetry/semantic-conventions';

import { AuthService } from './auth.service'; // adjust path if needed
import { environment } from '../../environments/environment'; // adjust path if needed

@Injectable({
  providedIn: 'root'
})
export class TracingService implements OnDestroy {

  private meterProvider!: SdkMeterProvider;
  private currentUser: { username?: string; role?: string } = {};
  private authSubscription!: Subscription;

  // Tab time tracking
  private currentTab: string = '';
  private tabEnteredAt: number = 0;

  // Meters
  private buttonClickCounter: any;
  private formSubmitCounter: any;
  private tabSwitchCounter: any;
  private tabTimeHistogram: any;

  constructor(private authService: AuthService) {}

  init(serviceName: string = environment.serviceName): void {
    // 1. Setup exporter → your OTel Collector OTLP HTTP endpoint
    const exporter = new OTLPMetricExporter({
      url: `${environment.collectorUrl}/v1/metrics`,
      headers: {},
    });

    // 2. Setup MeterProvider with resource and periodic export
    this.meterProvider = new SdkMeterProvider({
      resource: new Resource({
        [SEMRESATTRS_SERVICE_NAME]: serviceName,
      }),
      readers: [
        new PeriodicExportingMetricReader({
          exporter,
          exportIntervalMillis: 10_000, // export every 10 seconds
        }),
      ],
    });

    // 3. Register globally
    metrics.setGlobalMeterProvider(this.meterProvider);

    // 4. Create instruments
    const meter = this.meterProvider.getMeter(serviceName);

    this.buttonClickCounter = meter.createCounter('frontend.button.clicks', {
      description: 'Counts button click events',
    });

    this.formSubmitCounter = meter.createCounter('frontend.form.submits', {
      description: 'Counts form submit events',
    });

    this.tabSwitchCounter = meter.createCounter('frontend.tab.switches', {
      description: 'Counts tab navigation events',
    });

    this.tabTimeHistogram = meter.createHistogram('frontend.tab.time_spent_ms', {
      description: 'Time spent on each tab in milliseconds',
    });

    // 5. Subscribe to user identity once — all subsequent track calls carry it automatically
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.currentUser = {
        username: user?.username ?? 'anonymous',
        role: user?.role ?? 'anonymous',
      };
    });
  }

  // ─── Public tracking methods ────────────────────────────────────────────────

  /**
   * Call this on every tab/page switch.
   * Automatically calculates time spent on the previous tab.
   *
   * Usage: this.tracingService.trackTabSwitch(this.activeTab, newTab)
   */
  trackTabSwitch(from: string, to: string): void {
    const now = Date.now();

    // Record time spent on the tab we're leaving
    if (from && this.tabEnteredAt > 0) {
      const timeSpentMs = now - this.tabEnteredAt;
      this.tabTimeHistogram.record(timeSpentMs, {
        ...this.getUserAttributes(),
        'tab.name': from,
      });
    }

    // Count the navigation event
    this.tabSwitchCounter.add(1, {
      ...this.getUserAttributes(),
      'tab.from': from,
      'tab.to': to,
    });

    // Start tracking time on the new tab
    this.currentTab = to;
    this.tabEnteredAt = now;
  }

  /**
   * Call this on any meaningful button click.
   * Pass any relevant context as the second argument.
   *
   * Usage: this.tracingService.trackButtonClick('add-to-cart', { productId: 5, productName: 'Shoes', price: 49.99 })
   */
  trackButtonClick(buttonId: string, context: Record<string, string | number | boolean> = {}): void {
    this.buttonClickCounter.add(1, {
      ...this.getUserAttributes(),
      'button.id': buttonId,
      ...this.flattenContext(context),
    });
  }

  /**
   * Call this on form submission.
   * Pass safe, non-sensitive form parameters.
   *
   * Usage: this.tracingService.trackFormSubmit('checkout', { itemCount: 3, total: 49.99 })
   */
  trackFormSubmit(formName: string, params: Record<string, string | number | boolean> = {}): void {
    this.formSubmitCounter.add(1, {
      ...this.getUserAttributes(),
      'form.name': formName,
      ...this.flattenContext(params),
    });
  }

  // ─── Private helpers ─────────────────────────────────────────────────────────

  private getUserAttributes(): Record<string, string> {
    return {
      'user.username': this.currentUser.username ?? 'anonymous',
      'user.role': this.currentUser.role ?? 'anonymous',
    };
  }

  /**
   * Flattens context object to string values for OTel attributes.
   * Prefixes keys to avoid collisions e.g. { productId: 5 } → { 'ctx.productId': '5' }
   */
  private flattenContext(context: Record<string, string | number | boolean>): Record<string, string> {
    return Object.fromEntries(
      Object.entries(context).map(([k, v]) => [`ctx.${k}`, String(v)])
    );
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
    this.meterProvider?.shutdown();
  }
}