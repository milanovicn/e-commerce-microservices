import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

interface CartItem {
  product: Product;
  quantity: number;
}

interface Order {
  id: number;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: string;
  items: OrderItem[];
  createdAt: string;
}

interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

interface Notification {
  id: number;
  orderId: number;
  recipientEmail: string;
  message: string;
  notificationType: string;
  sentAt: string;
}

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.css']
})
export class MainComponent implements OnInit {
  activeTab = 'products';
  
  // Products
  products: Product[] = [];
  cart: CartItem[] = [];
  
  // Order Form
  customerName = '';
  customerEmail = '';
  
  // Orders
  orders: Order[] = [];
  
  // Notifications
  notifications: Notification[] = [];
  
  // UI State
  loading = false;
  message = '';
  messageType: 'success' | 'error' | '' = '';
  
  // API URLs
  private readonly PRODUCT_API = '/api/products';
  private readonly ORDER_API = '/api/orders';
  private readonly NOTIFICATION_API = '/api/notifications';
  
  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadProducts();
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    this.clearMessage();
    
    if (tab === 'orders') {
      this.loadOrders();
    } else if (tab === 'notifications') {
      this.loadNotifications();
    }
  }

  loadProducts() {
    this.loading = true;
    this.http.get<Product[]>(this.PRODUCT_API).subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (error) => {
        this.showMessage('Failed to load products: ' + error.message, 'error');
        this.loading = false;
      }
    });
  }

  addToCart(product: Product) {
    const existingItem = this.cart.find(item => item.product.id === product.id);
    
    if (existingItem) {
      if (existingItem.quantity < product.stockQuantity) {
        existingItem.quantity++;
        this.showMessage(`Added another ${product.name} to cart`, 'success');
      } else {
        this.showMessage('Not enough stock available', 'error');
      }
    } else {
      this.cart.push({ product, quantity: 1 });
      this.showMessage(`${product.name} added to cart`, 'success');
    }
  }

  removeFromCart(index: number) {
    const item = this.cart[index];
    this.cart.splice(index, 1);
    this.showMessage(`${item.product.name} removed from cart`, 'success');
  }

  getCartTotal(): number {
    return this.cart.reduce((total, item) => 
      total + (item.product.price * item.quantity), 0
    );
  }

  placeOrder() {
    if (!this.customerName || !this.customerEmail) {
      this.showMessage('Please enter your name and email', 'error');
      return;
    }

    if (this.cart.length === 0) {
      this.showMessage('Your cart is empty', 'error');
      return;
    }

    const orderRequest = {
      customerName: this.customerName,
      customerEmail: this.customerEmail,
      items: this.cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    this.loading = true;
    this.http.post<Order>(this.ORDER_API, orderRequest).subscribe({
      next: (order) => {
        this.showMessage(
          `Order #${order.id} placed successfully! Total: $${order.totalAmount.toFixed(2)}`,
          'success'
        );
        this.cart = [];
        this.loadProducts();
        this.loading = false;
        
        setTimeout(() => {
          this.setActiveTab('orders');
        }, 2000);
      },
      error: (error) => {
        this.showMessage('Failed to place order: ' + error.error?.message || error.message, 'error');
        this.loading = false;
      }
    });
  }

  loadOrders() {
    this.loading = true;
    this.http.get<Order[]>(this.ORDER_API).subscribe({
      next: (data) => {
        this.orders = data.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.loading = false;
      },
      error: (error) => {
        this.showMessage('Failed to load orders: ' + error.message, 'error');
        this.loading = false;
      }
    });
  }

  loadNotifications() {
    this.loading = true;
    this.http.get<Notification[]>(this.NOTIFICATION_API).subscribe({
      next: (data) => {
        this.notifications = data.sort((a, b) => 
          new Date(b.sentAt).getTime() - new Date(a.sentAt).getTime()
        );
        this.loading = false;
      },
      error: (error) => {
        this.showMessage('Failed to load notifications: ' + error.message, 'error');
        this.loading = false;
      }
    });
  }

  showMessage(message: string, type: 'success' | 'error') {
    this.message = message;
    this.messageType = type;
    setTimeout(() => this.clearMessage(), 5000);
  }

  clearMessage() {
    this.message = '';
    this.messageType = '';
  }
}