import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { QuoteService } from '../../services/quote.service';
import { ProductService } from '../../services/product.service';
import { QuoteResponse } from '../../models/quote.model';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-quote-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './quote-list.component.html',
  styleUrl: './quote-list.component.css'
})
export class QuoteListComponent implements OnInit {
  quotes: QuoteResponse[] = [];
  filteredQuotes: QuoteResponse[] = [];
  products: Product[] = [];
  loading = false;
  errorMessage: string | null = null;

  selectedProductId: number | null = null;
  minPrice: number | null = null;

  sortField: 'date' | 'price' = 'date';
  sortDirection: 'asc' | 'desc' = 'desc';

  currentPage = 0;
  pageSize = 2;
  totalItems = 0;

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  constructor(
    private quoteService: QuoteService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (products) => this.products = products,
      error: () => this.errorMessage = 'Failed to load products'
    });
    this.loadQuotes();
  }

  private loadQuotes(): void {
    this.loading = true;
    this.errorMessage = null;
    const filters: any = {};
    if (this.selectedProductId) filters.productId = this.selectedProductId;
    if (this.minPrice) filters.minPrice = this.minPrice;

    this.quoteService.getQuotes(filters).subscribe({
      next: (quotes) => {
        this.quotes = quotes;
        this.totalItems = quotes.length;
        this.sortQuotes();
        this.paginate();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load quotes';
        this.loading = false;
      }
    });
  }

  private paginate(): void {
    const sorted = [...this.quotes];
    this.filteredQuotes = sorted.slice(
      this.currentPage * this.pageSize,
      (this.currentPage + 1) * this.pageSize
    );
  }

  private sortQuotes(): void {
    this.quotes.sort((a, b) => {
      let comparison = 0;
      if (this.sortField === 'date') {
        comparison = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else {
        comparison = a.finalPrice - b.finalPrice;
      }
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadQuotes();
  }

  resetFilters(): void {
    this.selectedProductId = null;
    this.minPrice = null;
    this.currentPage = 0;
    this.loadQuotes();
  }

  changeSortField(field: 'date' | 'price'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.sortQuotes();
    this.paginate();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.paginate();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.paginate();
    }
  }

  viewQuote(id: number): void {
    this.router.navigate(['/quotes', id]);
  }
}