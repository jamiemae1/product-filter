import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import axios from 'axios';
import { RecentlyViewed } from './recently-viewed';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock FontAwesome icons
jest.mock('@fortawesome/react-fontawesome', () => ({
  FontAwesomeIcon: ({ icon, className }: { icon: any; className?: string }) => (
    <span data-testid={`icon-${icon.iconName}`} className={className} />
  ),
}));

// Wrapper component for router context
const RouterWrapper = ({ children }: { children: React.ReactNode }) => <BrowserRouter>{children}</BrowserRouter>;

describe('RecentlyViewed Component', () => {
  const mockRecentlyViewedProducts = [
    {
      id: 1,
      name: 'Recently Viewed Product 1',
      description: 'Description 1',
      price: 59.99,
      imageUrl: 'https://example.com/recent1.jpg',
      rating: 3.5,
      category: { id: 1, name: 'Electronics', description: 'Electronic devices' },
      user: { id: 1, login: 'testuser1' },
    },
    {
      id: 2,
      name: 'Recently Viewed Product 2',
      description: 'Description 2',
      price: 89.99,
      imageUrl: null,
      rating: null,
      category: { id: 2, name: 'Books', description: 'Books and literature' },
      user: { id: 2, login: 'testuser2' },
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    // Default successful API responses
    mockedAxios.get.mockResolvedValue({ data: mockRecentlyViewedProducts });
    mockedAxios.post.mockResolvedValue({ data: {} });
    mockedAxios.delete.mockResolvedValue({ data: {} });
  });

  describe('Component Rendering', () => {
    it('should render the component with header', () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      expect(screen.getByText('Recently Viewed Products')).toBeInTheDocument();
    });

    it('should display loading state initially', () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      expect(screen.getByText('Loading recently viewed products...')).toBeInTheDocument();
    });

    it('should render recently viewed products after loading', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getAllByText('Recently Viewed Product 1')[0]).toBeInTheDocument();
        expect(screen.getAllByText('Recently Viewed Product 2')[0]).toBeInTheDocument();
      });

      // Check product details - using getAllByText since prices appear in both desktop and mobile layouts
      expect(screen.getAllByText('$59.99')).toHaveLength(2);
      expect(screen.getAllByText('$89.99')).toHaveLength(2);
    });

    it('should show product count badge when products exist', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('2')).toBeInTheDocument();
      });
    });

    it('should show clear button when products exist', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Clear')).toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should show no recently viewed message when no products', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });

      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('No recently viewed products')).toBeInTheDocument();
        expect(screen.getByText('Products you view will appear here')).toBeInTheDocument();
      });
    });

    it('should not show clear button when no products', async () => {
      mockedAxios.get.mockResolvedValue({ data: [] });

      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.queryByText('Clear')).not.toBeInTheDocument();
      });
    });
  });

  describe('API Interactions', () => {
    it('should call API to load recently viewed products on mount', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledWith('/api/products/recently-viewed');
      });
    });

    it('should reload products when refreshTrigger prop changes', async () => {
      const { rerender } = render(<RecentlyViewed refreshTrigger={0} />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledTimes(1);
      });

      // Change the refresh trigger
      rerender(<RecentlyViewed refreshTrigger={1} />);

      await waitFor(() => {
        expect(mockedAxios.get).toHaveBeenCalledTimes(2);
      });
    });

    it('should track product view when product is clicked', async () => {
      const user = userEvent.setup();
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getAllByText('Recently Viewed Product 1')[0]).toBeInTheDocument();
      });

      const productLink = screen.getAllByText('Recently Viewed Product 1')[0];
      await user.click(productLink);

      await waitFor(() => {
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/products/recently-viewed', null, {
          params: { productName: 'Recently Viewed Product 1' },
        });
      });
    });

    it('should clear recently viewed products when clear button is clicked', async () => {
      const user = userEvent.setup();
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Clear')).toBeInTheDocument();
      });

      const clearButton = screen.getByText('Clear');
      await user.click(clearButton);

      await waitFor(() => {
        expect(mockedAxios.delete).toHaveBeenCalledWith('/api/products/recently-viewed');
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      mockedAxios.get.mockRejectedValueOnce(new Error('API Error'));

      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Failed to load recently viewed products')).toBeInTheDocument();
      });
    });

    it('should handle API errors when clearing products', async () => {
      const user = userEvent.setup();
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Clear')).toBeInTheDocument();
      });

      // Mock error for clear operation
      mockedAxios.delete.mockRejectedValueOnce(new Error('Clear Error'));

      const clearButton = screen.getByText('Clear');
      await user.click(clearButton);

      await waitFor(() => {
        expect(screen.getByText('Failed to clear recently viewed products')).toBeInTheDocument();
      });
    });
  });

  describe('Product Display', () => {
    it('should display product ratings correctly', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('(3.5)')).toBeInTheDocument();
        expect(screen.getByText('(N/A)')).toBeInTheDocument();
      });
    });

    it('should display product categories as badges', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getByText('Electronics')).toBeInTheDocument();
        expect(screen.getByText('Books')).toBeInTheDocument();
      });
    });

    it('should handle products without images', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        expect(screen.getAllByText('No Image')).toHaveLength(2); // Both desktop and mobile versions
      });
    });

    it('should render star ratings correctly for products with ratings', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        // Check that star icons are rendered (both desktop and mobile layouts render stars)
        // 5 stars per product x 2 products x 2 layouts (desktop + mobile) = 20 stars
        expect(screen.getAllByTestId('icon-star')).toHaveLength(20);
      });
    });
  });

  describe('Responsive Design', () => {
    it('should render desktop layout', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        const desktopContainer = document.querySelector('.d-none.d-md-block');
        expect(desktopContainer).toBeInTheDocument();
      });
    });

    it('should render mobile layout', async () => {
      render(<RecentlyViewed />, { wrapper: RouterWrapper });

      await waitFor(() => {
        const mobileContainer = document.querySelector('.d-md-none');
        expect(mobileContainer).toBeInTheDocument();
      });
    });
  });
});
