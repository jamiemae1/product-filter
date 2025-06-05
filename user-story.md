**Title**: Enhanced Product Filtering and Recently Viewed Products

**As a** online shopper
**I want** to filter products by price range, category, and customer ratings, and view recently viewed products
**So that** I can quickly find products that match my preferences and budget without browsing through irrelevant items, and easily return to items I previously explored.

**Business Logic**:

- Price range filtering should allow for a minimum and maximum price input.
- Category filtering should allow for single or multiple category selection.
- Customer rating filtering should allow selection of a minimum rating (e.g., 4 stars and up).
- Filters should be combinable (e.g., filter by price AND category AND rating).
- Recently viewed products should persist during the session and update as users browse product detail pages.
- Recently viewed products should not be duplicated and should maintain the order of most recent views.

**Acceptance Criteria**:

1. I can successfully filter products by a specific price range (e.g., $20-$50).
2. I can successfully filter products by one or more categories (e.g., "Electronics" and "Home Goods").
3. I can successfully filter products by a minimum customer rating (e.g., 4 stars and above).
4. I can successfully combine filters (e.g., products in the "Electronics" category, priced between $50 and $100, with a rating of 4 stars or higher).
5. The product list updates dynamically as I apply and remove filters.
6. If no products match the selected filters, a clear message is displayed indicating that no results were found.
7. I can view a list of my recently viewed products on the product listing page or in a dedicated section.
8. Recently viewed products show accurate product titles, thumbnails, and prices.
9. Clicking a recently viewed product takes me to its detail page.
10. Recently viewed products persist for the duration of my session and reset when I leave or log out.

**Functional Requirements**:

- Implement a price range filter with input fields for minimum and maximum price.
- Implement a category filter using a selectable list of available categories.
- Implement a customer rating filter using a selectable list of minimum rating options (e.g., 1 star, 2 stars, 3 stars, 4 stars, 5 stars).
- Implement a mechanism to apply and clear all filters.
- Display the number of products matching the current filter criteria.
- Track and display a list of recently viewed products (store in session or browser local storage).
- Ensure duplicates are not stored in the recently viewed list.
- Provide a section or component to render recently viewed products in a visually consistent and accessible way.

**Non-Functional Requirements**:

- The filtering process should be responsive and provide results within a reasonable timeframe (e.g., under 2 seconds).
- The filtering mechanism should be scalable to handle a large number of products and categories.
- The filtering mechanism should be accessible to users with disabilities, adhering to accessibility guidelines (e.g., WCAG).
- Recently viewed product tracking should not negatively impact application performance.

**UI Design**:

- Filters should be displayed prominently on the product listing page, ideally in a sidebar or collapsible panel.
- Price range filter should use input fields with appropriate validation to ensure numeric values.
- Category filter should use a checkbox list or a multi-select dropdown.
- Customer rating filter should use a radio button or star rating selection.
- A clear "Apply Filters" button should be present to trigger the filtering process.
- A "Clear Filters" button should be present to reset all filters to their default state.
- The "Recently Viewed Products" section should appear as a horizontal scrollable row, sidebar widget, or collapsible panel.
- Each recently viewed item should display its image, name, price, and a quick link to view more.
