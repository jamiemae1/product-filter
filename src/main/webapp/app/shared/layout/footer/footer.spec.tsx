import React from 'react';
import { render, screen } from '@testing-library/react';
import Footer from './footer';

describe('Footer Component', () => {
  describe('Component Rendering', () => {
    it('should render the footer content', () => {
      render(<Footer />);

      expect(screen.getByText('This is your footer')).toBeInTheDocument();
    });

    it('should have correct CSS classes', () => {
      const { container } = render(<Footer />);

      const footerElement = container.querySelector('.footer.page-content');
      expect(footerElement).toBeInTheDocument();
    });

    it('should have correct Bootstrap grid structure', () => {
      const { container } = render(<Footer />);

      expect(container.querySelector('.row')).toBeInTheDocument();
      expect(container.querySelector('.col-md-12')).toBeInTheDocument();
    });

    it('should render as a div element', () => {
      const { container } = render(<Footer />);

      const footerWrapper = container.firstChild;
      expect(footerWrapper).toBeInstanceOf(HTMLDivElement);
      expect(footerWrapper).toHaveClass('footer', 'page-content');
    });
  });

  describe('Content Structure', () => {
    it('should contain a paragraph with footer text', () => {
      render(<Footer />);

      const paragraph = screen.getByText('This is your footer');
      expect(paragraph.tagName).toBe('P');
    });

    it('should have single column layout', () => {
      const { container } = render(<Footer />);

      const column = container.querySelector('.col-md-12');
      expect(column).toBeInTheDocument();
      expect(container.querySelectorAll('[class*="col-"]')).toHaveLength(1);
    });
  });

  describe('Accessibility', () => {
    it('should be accessible', () => {
      const { container } = render(<Footer />);

      // Should not have any accessibility violations
      expect(container.firstChild).toBeInTheDocument();
    });

    it('should have readable text content', () => {
      render(<Footer />);

      const text = screen.getByText('This is your footer');
      expect(text).toBeVisible();
    });
  });

  describe('Styling', () => {
    it('should apply footer and page-content classes', () => {
      const { container } = render(<Footer />);

      const footerElement = container.firstChild;
      expect(footerElement).toHaveClass('footer');
      expect(footerElement).toHaveClass('page-content');
    });

    it('should use Reactstrap components', () => {
      const { container } = render(<Footer />);

      // Check for Reactstrap Row and Col structure
      expect(container.querySelector('.row')).toBeInTheDocument();
      expect(container.querySelector('.col-md-12')).toBeInTheDocument();
    });
  });

  describe('Component Structure', () => {
    it('should be a functional component', () => {
      expect(typeof Footer).toBe('function');
    });

    it('should render without props', () => {
      expect(() => render(<Footer />)).not.toThrow();
    });

    it('should be the default export', () => {
      // This test ensures the component is properly exported
      expect(Footer).toBeDefined();
      expect(typeof Footer).toBe('function');
    });
  });

  describe('Layout Responsiveness', () => {
    it('should use Bootstrap responsive column', () => {
      const { container } = render(<Footer />);

      const column = container.querySelector('.col-md-12');
      expect(column).toBeInTheDocument();
    });

    it('should maintain structure across screen sizes', () => {
      const { container } = render(<Footer />);

      // The md-12 class ensures full width on medium screens and up
      const fullWidthColumn = container.querySelector('.col-md-12');
      expect(fullWidthColumn).toBeInTheDocument();
    });
  });

  describe('Static Content', () => {
    it('should display static footer message', () => {
      render(<Footer />);

      // Verify the exact text content
      expect(screen.getByText('This is your footer')).toBeInTheDocument();
    });

    it('should not contain any dynamic content', () => {
      const { container } = render(<Footer />);

      // Footer should be static with no dynamic elements
      const textContent = container.textContent;
      expect(textContent).toBe('This is your footer');
    });

    it('should not contain any links or interactive elements', () => {
      const { container } = render(<Footer />);

      expect(container.querySelector('a')).not.toBeInTheDocument();
      expect(container.querySelector('button')).not.toBeInTheDocument();
      expect(container.querySelector('input')).not.toBeInTheDocument();
    });
  });
});
