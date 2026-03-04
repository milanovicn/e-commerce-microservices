import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Product {
  id: number;
  name: string;
  stockQuantity: number;
  price: number;
}

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  products: Product[] = [];
  selectedProduct: Product | null = null;
  restockQuantity = 0;
  successMessage = '';
  errorMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.http.get<Product[]>('/api/products').subscribe({
      next: (products) => {
        this.products = products;
      },
      error: (error) => {
        console.error('Failed to load products', error);
      }
    });
  }

  selectProduct(product: Product): void {
    this.selectedProduct = product;
    this.restockQuantity = 0;
    this.successMessage = '';
    this.errorMessage = '';
  }

  restock(): void {
    if (!this.selectedProduct || this.restockQuantity <= 0) {
      this.errorMessage = 'Please enter a valid quantity';
      return;
    }

    this.http.post(
      `/api/products/${this.selectedProduct.id}/restock?quantity=${this.restockQuantity}`,
      {}
    ).subscribe({
      next: () => {
        this.successMessage = `Successfully restocked ${this.selectedProduct!.name} with ${this.restockQuantity} units`;
        this.errorMessage = '';
        this.loadProducts();
        this.selectedProduct = null;
        this.restockQuantity = 0;
      },
      error: (error) => {
        console.error('Restock failed', error);
        this.errorMessage = 'Failed to restock product';
        this.successMessage = '';
      }
    });
  }
}