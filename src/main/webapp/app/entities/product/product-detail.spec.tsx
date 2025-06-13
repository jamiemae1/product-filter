import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import configureMockStore from 'redux-mock-store';
import axios from 'axios';
import { ProductDetail } from './product-detail';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock FontAwesome icons
jest.mock('@fortawesome/react-fontawesome', () => ({
  FontAwesomeIcon: ({ icon, className }: { icon: any; className?: string }) => <span data-testid={`icon-${icon}`} className={className} />,
}));

// Mock the product reducer actions
jest.mock('./product.reducer', () => ({
  getEntity: jest.fn(() => ({ type: 'GET_ENTITY', payload: { id: '1' } })),
}));

const mockStore = configureMockStore();

// Test data
const mockProductEntity = {
  id: 1,
  name: 'Test Product',
  description: 'Test Description',
  price: 99.99,
  imageUrl: 'http://example.com/image.jpg',
  rating: 4.5,
  category: {
    id: 1,
    name: 'Electronics',
  },
  user: {
    id: 1,
    login: 'testuser',
  },
};

// Wrapper component for providers
const TestWrapper = ({
  children,
  store,
  initialRoute = '/product/1',
}: {
  children: React.ReactNode;
  store: any;
  initialRoute?: string;
}) => (
  <Provider store={store}>
    <MemoryRouter initialEntries={[initialRoute]}>{children}</MemoryRouter>
  </Provider>
);

describe('ProductDetail Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedAxios.post.mockResolvedValue({ data: {} });
  });

  describe('Component Rendering', () => {
    it('should render the product detail heading', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Component uses data-cy="productDetailsHeading", not data-testid
      expect(screen.getByText('Product')).toBeInTheDocument();
      const heading = screen.getByRole('heading', { name: 'Product' });
      expect(heading).toBeInTheDocument();
    });

    it('should render all product fields with labels', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Check field labels
      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Description')).toBeInTheDocument();
      expect(screen.getByText('Price')).toBeInTheDocument();
      expect(screen.getByText('Image Url')).toBeInTheDocument();
      expect(screen.getByText('Rating')).toBeInTheDocument();
      expect(screen.getAllByText('Category')[0]).toBeInTheDocument();
      expect(screen.getAllByText('User')[0]).toBeInTheDocument();
    });

    it('should display product data correctly', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Check product data
      expect(screen.getByText('1')).toBeInTheDocument();
      expect(screen.getByText('Test Product')).toBeInTheDocument();
      expect(screen.getByText('Test Description')).toBeInTheDocument();
      expect(screen.getByText('99.99')).toBeInTheDocument();
      expect(screen.getByText('http://example.com/image.jpg')).toBeInTheDocument();
      expect(screen.getByText('4.5')).toBeInTheDocument();
      expect(screen.getByText('Electronics')).toBeInTheDocument();
      expect(screen.getByText('testuser')).toBeInTheDocument();
    });

    it('should render action buttons', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Check buttons using data-cy selectors - component uses data-cy not data-testid
      expect(screen.getByRole('link', { name: /Back/ })).toBeInTheDocument();
      expect(screen.getByText('Back')).toBeInTheDocument();
      expect(screen.getByText('Edit')).toBeInTheDocument();
    });

    it('should have correct button links', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      const backButton = screen.getByRole('link', { name: /Back/ });
      expect(backButton).toHaveAttribute('href', '/product');

      const editButton = screen.getByRole('link', { name: /Edit/i });
      expect(editButton).toHaveAttribute('href', '/product/1/edit');
    });
  });

  describe('Product Tracking', () => {
    it('should track product view when component loads with product data', async () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      await waitFor(() => {
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/products/recently-viewed', null, {
          params: { productName: 'Test Product' },
        });
      });
    });

    it('should not track view if product has no name', async () => {
      const productWithoutName = { ...mockProductEntity, name: null };
      const store = mockStore({
        product: {
          entity: productWithoutName,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Wait a bit to ensure no call is made
      await new Promise(resolve => setTimeout(resolve, 100));
      expect(mockedAxios.post).not.toHaveBeenCalled();
    });

    it('should handle tracking errors gracefully', async () => {
      mockedAxios.post.mockRejectedValueOnce(new Error('Tracking error'));
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      await waitFor(() => {
        expect(consoleSpy).toHaveBeenCalledWith('Error tracking product view:', expect.any(Error));
      });

      consoleSpy.mockRestore();
    });
  });

  describe('Data Handling', () => {
    it('should handle empty product entity', () => {
      const store = mockStore({
        product: {
          entity: {},
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      expect(screen.getByText('Product')).toBeInTheDocument();
      // Should not crash when displaying empty data
    });

    it('should handle missing category', () => {
      const productWithoutCategory = { ...mockProductEntity, category: null };
      const store = mockStore({
        product: {
          entity: productWithoutCategory,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Should display empty string for missing category
      const categoryElements = screen.getAllByText('Category');
      expect(categoryElements[0]).toBeInTheDocument();
    });

    it('should handle missing user', () => {
      const productWithoutUser = { ...mockProductEntity, user: null };
      const store = mockStore({
        product: {
          entity: productWithoutUser,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Should display empty string for missing user
      const userElements = screen.getAllByText('User');
      expect(userElements[0]).toBeInTheDocument();
    });

    it('should display null values appropriately', () => {
      const productWithNulls = {
        ...mockProductEntity,
        description: null,
        imageUrl: null,
        rating: null,
      };

      const store = mockStore({
        product: {
          entity: productWithNulls,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Component should handle null values without crashing
      expect(screen.getByText('Test Product')).toBeInTheDocument();
    });
  });

  describe('Route Parameters', () => {
    it('should work with different product IDs in route', () => {
      const store = mockStore({
        product: {
          entity: { ...mockProductEntity, id: 123 },
        },
      });

      render(
        <TestWrapper store={store} initialRoute="/product/123">
          <ProductDetail />
        </TestWrapper>,
      );

      expect(screen.getByText('123')).toBeInTheDocument();
    });
  });

  describe('Button Styling', () => {
    it('should have correct button colors and icons', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      const backButton = screen.getByRole('link', { name: /Back/ });
      expect(backButton).toHaveClass('btn-info');

      // Check for FontAwesome icon mock
      expect(screen.getByTestId('icon-arrow-left')).toBeInTheDocument();
      expect(screen.getByTestId('icon-pencil-alt')).toBeInTheDocument();
    });

    it('should hide button text on mobile', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      // Check for d-none d-md-inline classes
      const hiddenTextElements = container.querySelectorAll('.d-none.d-md-inline');
      expect(hiddenTextElements.length).toBeGreaterThan(0);
    });
  });

  describe('Layout Structure', () => {
    it('should have correct Bootstrap grid structure', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      expect(container.querySelector('.row')).toBeInTheDocument();
      expect(container.querySelector('.col-md-8')).toBeInTheDocument();
    });

    it('should use definition list for product details', () => {
      const store = mockStore({
        product: {
          entity: mockProductEntity,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <ProductDetail />
        </TestWrapper>,
      );

      expect(container.querySelector('dl.jh-entity-details')).toBeInTheDocument();
      expect(container.querySelectorAll('dt')).toHaveLength(8); // 8 fields
      expect(container.querySelectorAll('dd')).toHaveLength(8); // 8 values
    });
  });
});
