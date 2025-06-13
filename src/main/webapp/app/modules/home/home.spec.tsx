import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import configureMockStore from 'redux-mock-store';
import { Home } from './home';

// Mock FontAwesome icons
jest.mock('@fortawesome/react-fontawesome', () => ({
  FontAwesomeIcon: ({ icon, className }: { icon: any; className?: string }) => (
    <span data-testid={`icon-${icon.iconName}`} className={className} />
  ),
}));

const mockStore = configureMockStore();

// Wrapper component for providers
const TestWrapper = ({ children, store }: { children: React.ReactNode; store: any }) => (
  <Provider store={store}>
    <BrowserRouter>{children}</BrowserRouter>
  </Provider>
);

describe('Home Component', () => {
  describe('Component Rendering', () => {
    it('should render the main welcome message and content', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      expect(screen.getByText('Welcome, Java Hipster!')).toBeInTheDocument();
      expect(screen.getByText('This is your homepage')).toBeInTheDocument();
      expect(screen.getByText('If you have any question on JHipster:')).toBeInTheDocument();
    });

    it('should render JHipster links and resources', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check external links
      expect(screen.getByText('JHipster homepage')).toBeInTheDocument();
      expect(screen.getByText('JHipster on Stack Overflow')).toBeInTheDocument();
      expect(screen.getByText('JHipster bug tracker')).toBeInTheDocument();
      expect(screen.getByText('JHipster public chat room')).toBeInTheDocument();
      expect(screen.getByText('follow @jhipster on Twitter')).toBeInTheDocument();

      // Check GitHub link
      expect(screen.getByText('GitHub')).toBeInTheDocument();
    });

    it('should have correct href attributes for external links', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      const homepageLink = screen.getByRole('link', { name: 'JHipster homepage' });
      expect(homepageLink).toHaveAttribute('href', 'https://www.jhipster.tech/');
      expect(homepageLink).toHaveAttribute('target', '_blank');
      expect(homepageLink).toHaveAttribute('rel', 'noopener noreferrer');

      const stackOverflowLink = screen.getByRole('link', { name: 'JHipster on Stack Overflow' });
      expect(stackOverflowLink).toHaveAttribute('href', 'https://stackoverflow.com/tags/jhipster/info');

      const bugTrackerLink = screen.getByRole('link', { name: 'JHipster bug tracker' });
      expect(bugTrackerLink).toHaveAttribute('href', 'https://github.com/jhipster/generator-jhipster/issues?state=open');

      const chatLink = screen.getByRole('link', { name: 'JHipster public chat room' });
      expect(chatLink).toHaveAttribute('href', 'https://gitter.im/jhipster/generator-jhipster');

      const twitterLink = screen.getByRole('link', { name: 'follow @jhipster on Twitter' });
      expect(twitterLink).toHaveAttribute('href', 'https://twitter.com/jhipster');
    });
  });

  describe('Authentication States', () => {
    it('should show login prompt when user is not authenticated', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check warning alerts for unauthenticated users
      expect(screen.getByText(/If you want to/)).toBeInTheDocument();
      expect(screen.getByText('sign in')).toBeInTheDocument();
      expect(screen.getByText(/Administrator \(login="admin" and password="admin"\)/)).toBeInTheDocument();
      expect(screen.getByText(/User \(login="user" and password="user"\)/)).toBeInTheDocument();

      // Check register prompt
      expect(screen.getByText(/You don't have an account yet?/)).toBeInTheDocument();
      expect(screen.getByText('Register a new account')).toBeInTheDocument();
    });

    it('should show welcome message when user is authenticated', () => {
      const mockAccount = {
        login: 'testuser',
        firstName: 'Test',
        lastName: 'User',
        email: 'test@example.com',
      };

      const store = mockStore({
        authentication: {
          account: mockAccount,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check authenticated user message
      expect(screen.getByText(/You are logged in as user "testuser"/)).toBeInTheDocument();

      // Should not show login/register prompts
      expect(screen.queryByText(/If you want to/)).not.toBeInTheDocument();
      expect(screen.queryByText(/You don't have an account yet?/)).not.toBeInTheDocument();
    });

    it('should display correct alert colors for different states', () => {
      const store = mockStore({
        authentication: {
          account: { login: 'testuser' },
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check for success alert when authenticated
      const successAlert = container.querySelector('.alert-success');
      expect(successAlert).toBeInTheDocument();
    });

    it('should show warning alerts for unauthenticated users', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check for warning alerts when not authenticated
      const warningAlerts = container.querySelectorAll('.alert-warning');
      expect(warningAlerts).toHaveLength(2); // Two warning alerts for login and register
    });
  });

  describe('Navigation Links', () => {
    it('should have working internal navigation links', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check login link
      const loginLink = screen.getByRole('link', { name: 'sign in' });
      expect(loginLink).toHaveAttribute('href', '/login');

      // Check register link
      const registerLink = screen.getByRole('link', { name: 'Register a new account' });
      expect(registerLink).toHaveAttribute('href', '/account/register');
    });

    it('should have alert-link class on internal links', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      const loginLink = screen.getByRole('link', { name: 'sign in' });
      expect(loginLink).toHaveClass('alert-link');

      const registerLink = screen.getByRole('link', { name: 'Register a new account' });
      expect(registerLink).toHaveClass('alert-link');
    });
  });

  describe('Layout Structure', () => {
    it('should have correct Bootstrap grid structure', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Check for row and column classes
      expect(container.querySelector('.row')).toBeInTheDocument();
      expect(container.querySelector('.col-md-3')).toBeInTheDocument();
      expect(container.querySelector('.col-md-9')).toBeInTheDocument();
    });

    it('should have hipster image element', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      const hipsterElement = container.querySelector('.hipster.rounded');
      expect(hipsterElement).toBeInTheDocument();
    });

    it('should have display-4 class on main heading', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      const { container } = render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      const mainHeading = screen.getByText('Welcome, Java Hipster!');
      expect(mainHeading).toHaveClass('display-4');
    });

    it('should have lead class on subtitle', () => {
      const store = mockStore({
        authentication: {
          account: null,
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      const subtitle = screen.getByText('This is your homepage');
      expect(subtitle).toHaveClass('lead');
    });
  });

  describe('Edge Cases', () => {
    it('should handle undefined account gracefully', () => {
      const store = mockStore({
        authentication: {
          account: undefined,
        },
      });

      expect(() => {
        render(
          <TestWrapper store={store}>
            <Home />
          </TestWrapper>,
        );
      }).not.toThrow();

      expect(screen.getByText(/If you want to/)).toBeInTheDocument();
    });

    it('should handle account with no login property', () => {
      const store = mockStore({
        authentication: {
          account: { firstName: 'Test', lastName: 'User' },
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Should show unauthenticated state since no login property
      expect(screen.getByText(/If you want to/)).toBeInTheDocument();
    });

    it('should handle empty login string', () => {
      const store = mockStore({
        authentication: {
          account: { login: '' },
        },
      });

      render(
        <TestWrapper store={store}>
          <Home />
        </TestWrapper>,
      );

      // Should show unauthenticated state for empty login
      expect(screen.getByText(/If you want to/)).toBeInTheDocument();
    });
  });
});
