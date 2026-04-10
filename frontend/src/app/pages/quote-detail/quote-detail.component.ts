import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { QuoteResponse } from '../../models/quote.model';

/**
 * Component for displaying the details of a single quote
 *
 * TODO: Candidate must implement the following:
 * 1. Get quote ID from route parameters (hint: this.route.snapshot.paramMap.get('id'))
 *
 * 2. Load quote details from QuoteService.getQuote(id)
 *
 * 3. Display complete quote information:
 *    - Client details (name, age)
 *    - Insurance details (product, zone)
 *    - Pricing breakdown (base price, applied rules, final price)
 *
 * 4. Handle loading state while fetching data
 *
 * 5. Handle error state if quote cannot be loaded
 */
@Component({
  selector: 'app-quote-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quote-detail.component.html',
  styleUrl: './quote-detail.component.css'
})
export class QuoteDetailComponent implements OnInit {
  quote: QuoteResponse | null = null;
  loading = false;
  errorMessage: string | null = null;

  constructor(
    private quoteService: QuoteService,
    private route: ActivatedRoute
  ) {}

ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');
  if (!id) {
    this.errorMessage = 'Quote ID not found';
    return;
  }

  this.loading = true;
  this.quoteService.getQuote(Number(id)).subscribe({
    next: (quote) => {
      this.quote = quote;
      this.loading = false;
    },
    error: () => {
      this.errorMessage = 'Failed to load quote details';
      this.loading = false;
    }
  });
}

downloadPdf(): void {
  if (!this.quote) return;
  this.quoteService.downloadQuotePdf(this.quote.quoteId).subscribe({
    next: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `quote-${this.quote!.quoteId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    },
    error: () => this.errorMessage = 'Failed to download PDF'
  });
}
}
