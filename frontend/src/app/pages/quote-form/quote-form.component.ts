import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

/**
 * Available zones with their codes (must match backend data.sql)
 */
const ZONES = [
  { code: 'TUN', name: 'Grand Tunis' },
  { code: 'SFX', name: 'Sfax' },
  { code: 'SOU', name: 'Sousse' }
];

/**
 * Component for creating a new quote
 *
 * TODO: Candidate must implement the following:
 * 1. Load products from ProductService on init and populate the product dropdown
 *
 * 2. Implement form submission in onSubmit():
 *    - Validate form before submission
 *    - Build a QuoteRequest from form values
 *    - Call QuoteService.createQuote()
 *    - Show success/error message
 *    - Navigate to quote detail page on success
 *
 * 3. Handle loading state while API request is in progress
 */
@Component({
  selector: 'app-quote-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './quote-form.component.html',
  styleUrl: './quote-form.component.css'
})
export class QuoteFormComponent implements OnInit {
  form: FormGroup;
  products: Product[] = [];
  zones = ZONES;
  loading = false;
  submitted = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private quoteService: QuoteService,
    private productService: ProductService,
    private router: Router
  ) {
    this.form = this.fb.group({
      clientName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      productId: ['', [Validators.required]],
      zoneCode: ['', [Validators.required]],
      clientAge: ['', [Validators.required, Validators.min(18), Validators.max(99)]]
    });
  }

ngOnInit(): void {
  this.productService.getProducts().subscribe({
    next: (products) => this.products = products,
    error: () => this.errorMessage = 'Failed to load products'
  });
}  /**
   * Submit the form
   *
   * TODO: Implement form submission
   * - Check if form is valid (mark all fields as touched if invalid)
   * - Set loading state
   * - Build QuoteRequest: { productId: number, zoneCode: string, clientName: string, clientAge: number }
   * - Call quoteService.createQuote(request)
   * - On success: show message, navigate to /quotes/{quoteId}
   * - On error: show error message
   * - Always reset loading state
   */
onSubmit(): void {
  this.submitted = true;
  if (this.form.invalid) return;

  this.loading = true;
  this.errorMessage = null;

  const request = {
    productId: Number(this.form.value.productId),
    zoneCode: this.form.value.zoneCode,
    clientName: this.form.value.clientName,
    clientAge: Number(this.form.value.clientAge)
  };

  this.quoteService.createQuote(request).subscribe({
    next: (response) => {
      this.loading = false;
      this.successMessage = 'Quote created successfully!';
      this.router.navigate(['/quotes', response.quoteId]);
    },
    error: (err) => {
      this.loading = false;
      this.errorMessage = err.message || 'Failed to create quote';
    }
  });
}

  /**
   * Check if a form field has an error (provided helper)
   */
  hasError(fieldName: string, errorType: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.hasError(errorType) && (field.dirty || field.touched || this.submitted));
  }

  /**
   * Check if a form field is invalid (provided helper)
   */
  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  /**
   * Get error message for a field (provided helper)
   */
  getErrorMessage(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (!field || !field.errors) return '';

    if (field.hasError('required')) return `This field is required`;
    if (field.hasError('minlength')) return `Minimum ${field.errors['minlength'].requiredLength} characters`;
    if (field.hasError('min')) return `Minimum value is ${field.errors['min'].min}`;
    if (field.hasError('max')) return `Maximum value is ${field.errors['max'].max}`;

    return 'Invalid input';
  }
}
