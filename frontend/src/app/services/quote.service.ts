import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { QuoteRequest, QuoteResponse } from '../models/quote.model';

@Injectable({
  providedIn: 'root'
})
export class QuoteService {
  private readonly apiUrl = environment.apiUrl;
  private readonly endpoint = '/quotes';

  constructor(private http: HttpClient) {}

  createQuote(request: QuoteRequest): Observable<QuoteResponse> {
    return this.http.post<QuoteResponse>(`${this.apiUrl}${this.endpoint}`, request)
      .pipe(catchError(this.handleError));
  }

  getQuote(id: number): Observable<QuoteResponse> {
    return this.http.get<QuoteResponse>(`${this.apiUrl}${this.endpoint}/${id}`)
      .pipe(catchError(this.handleError));
  }

  getQuotes(filters?: { productId?: number; minPrice?: number }): Observable<QuoteResponse[]> {
    let params = new HttpParams();
    if (filters?.productId) {
      params = params.set('productId', filters.productId.toString());
    }
    if (filters?.minPrice) {
      params = params.set('minPrice', filters.minPrice.toString());
    }
    return this.http.get<QuoteResponse[]>(`${this.apiUrl}${this.endpoint}`, { params })
      .pipe(catchError(this.handleError));
  }

  downloadQuotePdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}${this.endpoint}/${id}/pdf`, { responseType: 'blob' })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('Quote service error:', error);
    const message = error?.error?.message || 'Failed to process quote';
    return throwError(() => new Error(message));
  }
}