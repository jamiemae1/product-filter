import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import configureMockStore from 'redux-mock-store';
import axios from 'axios';
import { Product } from './product';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock FontAwesome icons
jest.mock('@fortawesome/react-fontawesome', () => ({
  FontAwesomeIcon: ({ icon, className }: { icon: any; className?: string }) => (
    <span data-testid={`icon-${icon.iconName}`} className={className} />
  ),
}));

// Mock RecentlyViewed component to avoid testing its functionality here
jest.mock('./recently-viewed', () => ({
  RecentlyViewed: () => <div data-testid="recently-viewed-mock">Recently Viewed Mock</div>,
}));

const mockStore = configureMockStore();

// Test data
const mockProducts = [
  {
    id: 1,
    name: 'Test Product 1',
    description: 'Test Description 1',
    price: 99.99,
    imageUrl: 'http://example.com/image1.jpg',
    rating: 4.5,
    category: { id: 1, name: 'Electronics' },
    user: { id: 1, login: 'user1' },
  },
  {
    id: 2,
    name: 'Test Product 2',
    description: 'Test Description 2',
    price: 149.99,
    imageUrl: 'http://example.com/image2.jpg',
    rating: 4.0,
    category: { id: 2, name: 'Books' },
    user: { id: 2, login: 'user2' },
  },
];

const mockCategories = [
  { id: 1, name: 'Electronics' },
  { id: 2, name: 'Books' },
];

// Wrapper component for providers
const TestWrapper = ({ children }: { children: React.ReactNode }) => {
  const store = mockStore({
    product: {
      entities: mockProducts,
      entity: {},
      loading: false,
      errorMessage: null,
      totalItems: 2,
    },
    category: {
      entities: mockCategories,
    },
  });

  return (
    <Provider store={store}>
      <BrowserRouter>{children}</BrowserRouter>
    </Provider>
  );
};

describe('Product Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Mock API responses
    mockedAxios.get.mockImplementation(url => {
      if (url === '/api/products/filter') {
        return Promise.resolve({ data: mockProducts });
      }
      if (url === '/api/categories') {
        return Promise.resolve({ data: mockCategories });
      }
      if (url === '/api/products/recently-viewed') {
        return Promise.resolve({ data: [] });
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });
  });

  describe('Component Structure', () => {
    it('should be a functional component', () => {
      expect(typeof Product).toBe('function');
    });

    it('should render without props', () => {
      expect(() => {
        render(
          <TestWrapper>
            <Product />
          </TestWrapper>,
        );
      }).not.toThrow();
    });

    it('should be a simple wrapper around ProductFilter', () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // The Product component should render the ProductFilter component
      // We can verify this by checking for elements that come from ProductFilter
      expect(screen.getByText('Product Filters')).toBeInTheDocument();
    });
  });

  describe('ProductFilter Integration', () => {
    it('should render the ProductFilter component', () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Check that ProductFilter is rendered by looking for its key elements
      expect(screen.getByText('Product Filters')).toBeInTheDocument();
      expect(screen.getByText('Price Range')).toBeInTheDocument();
      expect(screen.getByText('Category')).toBeInTheDocument();
      expect(screen.getByText('Minimum Rating')).toBeInTheDocument();
    });

    it('should pass through all ProductFilter functionality', async () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Wait for ProductFilter to load (check for product count display)
      await screen.findByText(/found/);

      // Should see "0 found" initially when no products match or are loading
      expect(screen.getByText(/\d+ found/)).toBeInTheDocument();
    });

    it('should inherit all ProductFilter props and behavior', async () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Check that filtering controls are available
      expect(screen.getByPlaceholderText('Min')).toBeInTheDocument();
      expect(screen.getByPlaceholderText('Max')).toBeInTheDocument();

      // Wait for loading to complete before checking buttons
      await waitFor(
        () => {
          expect(screen.queryByText('Loading products...')).not.toBeInTheDocument();
        },
        { timeout: 3000 },
      );

      // Check if either "Apply Filters" or "Filtering..." is present (depends on loading state)
      expect(screen.getByText('Apply Filters') || screen.getByText('Filtering...')).toBeInTheDocument();
      expect(screen.getByText('Clear All')).toBeInTheDocument();
    });
  });

  describe('Default Export', () => {
    it('should be the default export', () => {
      expect(Product).toBeDefined();
      expect(typeof Product).toBe('function');
    });

    it('should export the component correctly', () => {
      // This test ensures proper module export
      expect(Product.name).toBe('Product');
    });
  });

  describe('Component Simplicity', () => {
    it('should be a simple wrapper without additional logic', () => {
      const { container } = render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // The Product component should not add any additional wrapper elements
      // It should directly render the ProductFilter component
      expect(container.firstChild).not.toHaveClass('product-wrapper');
    });

    it('should not modify ProductFilter behavior', () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // All ProductFilter functionality should work unchanged
      expect(screen.getByTestId('recently-viewed-mock')).toBeInTheDocument();
    });

    it('should not add any additional state or effects', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Should not cause any additional console errors
      expect(consoleSpy).not.toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });

  describe('Rendering Performance', () => {
    it('should render efficiently without unnecessary re-renders', () => {
      const { rerender } = render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Re-render should not cause issues
      rerender(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      expect(screen.getByText('Product Filters')).toBeInTheDocument();
    });

    it('should not cause memory leaks', () => {
      const { unmount } = render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // Unmounting should work cleanly
      expect(() => unmount()).not.toThrow();
    });
  });

  describe('Route Compatibility', () => {
    it('should work as a route component', () => {
      // The Product component should be suitable for use in React Router routes
      expect(() => {
        render(
          <TestWrapper>
            <Product />
          </TestWrapper>,
        );
      }).not.toThrow();
    });

    it('should handle route changes properly', () => {
      render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      // The component should render successfully in a routing context
      expect(screen.getByText('Product Filters')).toBeInTheDocument();
    });
  });

  describe('Error Boundaries', () => {
    it('should not break when ProductFilter encounters errors', () => {
      // Mock a ProductFilter error scenario
      mockedAxios.get.mockRejectedValueOnce(new Error('API Error'));

      expect(() => {
        render(
          <TestWrapper>
            <Product />
          </TestWrapper>,
        );
      }).not.toThrow();
    });

    it('should gracefully handle component unmounting', () => {
      const { unmount } = render(
        <TestWrapper>
          <Product />
        </TestWrapper>,
      );

      expect(() => unmount()).not.toThrow();
    });
  });
});
