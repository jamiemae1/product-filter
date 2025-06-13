import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import axios from 'axios';
import { ProductFilter } from './product-filter';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock the RecentlyViewed component
jest.mock('./recently-viewed', () => ({
  RecentlyViewed: ({ refreshTrigger }: { refreshTrigger: number }) => (
    <div data-testid="recently-viewed-mock">Recently Viewed - Refresh: {refreshTrigger}</div>
  ),
}));

// Mock FontAwesome icons
jest.mock('@fortawesome/react-fontawesome', () => ({
  FontAwesomeIcon: ({ icon, className }: { icon: any; className?: string }) => (
    <span data-testid={`icon-${icon.iconName}`} className={className} />
  ),
}));

// Wrapper component for router context
const RouterWrapper = ({ children }: { children: React.ReactNode }) => <BrowserRouter>{children}</BrowserRouter>;

describe('ProductFilter Component', () => {
  const mockProducts = [
    {
      id: 1,
      name: 'Test Product 1',
      description: 'Test Description 1',
      price: 99.99,
      imageUrl: 'https://example.com/image1.jpg',
      rating: 4.5,
      category: { id: 1, name: 'Electronics', description: 'Electronic devices' },
      user: { id: 1, login: 'testuser1' },
    },
    {
      id: 2,
      name: 'Test Product 2',
      description: 'Test Description 2',
      price: 149.99,
      imageUrl: null,
      rating: null,
      category: { id: 2, name: 'Books', description: 'Books and literature' },
      user: { id: 2, login: 'testuser2' },
    },
  ];

  const mockCategories = [
    { id: 1, name: 'Electronics', description: 'Electronic devices' },
    { id: 2, name: 'Books', description: 'Books and literature' },
    { id: 3, name: 'Clothing', description: 'Apparel and accessories' },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    // Default successful API responses
    mockedAxios.get.mockImplementation(url => {
      if (url.includes('/api/products?eagerload=true')) {
        return Promise.resolve({ data: mockProducts });
      }
      if (url.includes('/api/categories')) {
        return Promise.resolve({ data: mockCategories });
      }
      if (url.includes('/api/products/filter/combined')) {
        return Promise.resolve({ data: mockProducts });
      }
      return Promise.resolve({ data: [] });
    });
    mockedAxios.post.mockResolvedValue({ data: {} });
  });

  describe('Component Rendering', () => {
    it('should render the component with filters and products', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      // Check filter section is rendered
      expect(screen.getByText('Product Filters')).toBeInTheDocument();
      expect(screen.getByText('Price Range')).toBeInTheDocument();

      // Wait for products to load
      await waitFor(() => {
        expect(screen.getByText('2 found')).toBeInTheDocument();
      });
    });

    it('should render filter form elements correctly', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Apply Filters')).toBeInTheDocument();
        expect(screen.getByText('Clear All')).toBeInTheDocument();
      });

      expect(screen.getByText('Price Range')).toBeInTheDocument();
      expect(screen.getByText('Category')).toBeInTheDocument();
      expect(screen.getByText('Minimum Rating')).toBeInTheDocument();
    });

    it('should render products after loading', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Test Product 1')).toBeInTheDocument();
        expect(screen.getByText('Test Product 2')).toBeInTheDocument();
      });

      // Check product details
      expect(screen.getByText('$99.99')).toBeInTheDocument();
      expect(screen.getByText('$149.99')).toBeInTheDocument();
      expect(screen.getAllByText('Electronics')).toHaveLength(2); // One in filter, one in badge
      expect(screen.getAllByText('Books')).toHaveLength(2); // One in filter, one in badge
    });
  });

  describe('User Interactions', () => {
    it('should update filter values when user types in price inputs', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      const minPriceInput = screen.getByPlaceholderText('Min');
      const maxPriceInput = screen.getByPlaceholderText('Max');

      await user.type(minPriceInput, '50');
      await user.type(maxPriceInput, '200');

      expect(minPriceInput).toHaveValue(50);
      expect(maxPriceInput).toHaveValue(200);
    });

    it('should update category filter when user selects category', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        const categorySelect = screen.getByDisplayValue('All Categories');
        expect(categorySelect).toBeInTheDocument();
      });

      const categorySelect = screen.getByDisplayValue('All Categories');
      await user.selectOptions(categorySelect, 'Electronics');

      expect(categorySelect).toHaveValue('Electronics');
    });

    it('should apply filters and call filter API', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Apply Filters')).toBeInTheDocument();
      });

      // Set some filters
      const minPriceInput = screen.getByPlaceholderText('Min');
      await user.type(minPriceInput, '50');

      const categorySelect = screen.getByDisplayValue('All Categories');
      await user.selectOptions(categorySelect, 'Electronics');

      // Apply filters
      const applyButton = screen.getByText('Apply Filters');
      await user.click(applyButton);

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/products/filter/combined?minPrice=50&categoryName=Electronics');
      });
    });

    it('should clear filters and reload all products', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      // Wait for initial load and set a filter
      await waitFor(() => {
        expect(screen.getByText('Clear All')).toBeInTheDocument();
      });

      const minPriceInput = screen.getByPlaceholderText('Min');
      await user.type(minPriceInput, '50');

      // Clear filters
      const clearButton = screen.getByText('Clear All');
      await user.click(clearButton);

      // Check that input is cleared
      expect(minPriceInput).toHaveValue(null);
    });
  });

  describe('API Interactions', () => {
    it('should call correct API endpoints on component mount', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/products?eagerload=true&size=1000');
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories?size=1000');
      });
    });

    it('should track product view when product link is clicked', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Test Product 1')).toBeInTheDocument();
      });

      const productLink = screen.getByText('Test Product 1');
      await user.click(productLink);

      await waitFor(() => {
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/products/recently-viewed', null, { params: { productName: 'Test Product 1' } });
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      // Mock API error
      mockedAxios.get.mockRejectedValueOnce(new Error('API Error'));

      render(<ProductFilter />, { wrapper: RouterWrapper });

      // Should still render the component without crashing
      await waitFor(() => {
        expect(screen.getByText('Product Filters')).toBeInTheDocument();
      });
    });

    it('should show no results message when no products match filters', async () => {
      // Mock empty response for filter
      mockedAxios.get.mockImplementation(url => {
        if (url.includes('/api/products/filter/combined')) {
          return Promise.resolve({ data: [] });
        }
        if (url.includes('/api/categories')) {
          return Promise.resolve({ data: mockCategories });
        }
        return Promise.resolve({ data: mockProducts });
      });

      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Apply Filters')).toBeInTheDocument();
      });

      // Apply filters that return no results
      const applyButton = screen.getByText('Apply Filters');
      await user.click(applyButton);

      await waitFor(() => {
        expect(screen.getByText(/No products found/)).toBeInTheDocument();
      });
    });
  });

  describe('Conditional Rendering', () => {
    it('should show N/A for products with null rating', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('(N/A)')).toBeInTheDocument();
      });
    });

    it('should show "No Image" placeholder when imageUrl is null', async () => {
      render(<ProductFilter />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('No Image')).toBeInTheDocument();
      });
    });
  });

  describe('Recently Viewed Integration', () => {
    it('should refresh recently viewed component when product is viewed', async () => {
      const user = userEvent.setup();
      render(<ProductFilter />, { wrapper: RouterWrapper });

      // Initial refresh trigger should be 0
      expect(screen.getByText('Recently Viewed - Refresh: 0')).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.getByText('Test Product 1')).toBeInTheDocument();
      });

      // Click on a product
      const productLink = screen.getByText('Test Product 1');
      await user.click(productLink);

      // Should update refresh trigger
      await waitFor(() => {
        expect(screen.getByText('Recently Viewed - Refresh: 1')).toBeInTheDocument();
      });
    });
  });
});
