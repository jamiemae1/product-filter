Feature: Enhanced Product Filtering and Recently Viewed Products
  As an online shopper
  I want to filter products by price range, category, and customer ratings, and view recently viewed products
  So that I can quickly find products that match my preferences and budget without browsing through irrelevant items, and easily return to items I previously explored

  Background:
    Given I have the following categories in the system:
      | name        | description          |
      | Electronics | Electronic devices   |
      | Home Goods  | Home and garden items|
      | Books       | Books and magazines  |
    And I have the following products in the system:
      | name           | price | category    | rating | description        |
      | Laptop         | 799.99| Electronics | 4.5    | Gaming laptop      |
      | Smartphone     | 299.99| Electronics | 4.2    | Android phone      |
      | Coffee Maker   | 89.99 | Home Goods  | 4.8    | Automatic brewer   |
      | Garden Hose    | 25.50 | Home Goods  | 3.9    | 50ft garden hose   |
      | Programming Book| 45.00| Books       | 4.7    | Learn Java         |
      | Fiction Novel  | 12.99 | Books       | 3.5    | Mystery novel      |
      | Tablet         | 199.99| Electronics | 4.1    | 10-inch tablet     |
      | Blender        | 65.00 | Home Goods  | 4.3    | High-speed blender |

  # Product Filtering Scenarios
  Scenario: Filter products by price range
    When I filter products with minimum price 50.00 and maximum price 300.00
    Then I should see 4 products
    And the products should include "Smartphone", "Coffee Maker", "Tablet", "Blender"

  Scenario: Filter products by single category
    When I filter products by category "Electronics"
    Then I should see 3 products
    And all products should belong to category "Electronics"

  Scenario: Filter products by multiple categories
    When I filter products by categories "Electronics" and "Home Goods"
    Then I should see 6 products
    And the products should belong to either "Electronics" or "Home Goods" category

  Scenario: Filter products by minimum rating
    When I filter products with minimum rating 4.0
    Then I should see 6 products
    And all products should have rating 4.0 or higher

  Scenario: Combine price and category filters
    When I filter products with minimum price 100.00 and maximum price 500.00
    And I filter products by category "Electronics"
    Then I should see 2 products
    And the products should include "Smartphone", "Tablet"

  Scenario: Combine price, category and rating filters
    When I filter products with minimum price 50.00 and maximum price 800.00
    And I filter products by category "Electronics"
    And I filter products with minimum rating 4.2
    Then I should see 2 products
    And the products should include "Laptop", "Smartphone"

  Scenario: Filter with no matching results
    When I filter products with minimum price 1000.00 and maximum price 2000.00
    Then I should see 0 products
    And I should see a "No products found" message

  Scenario: Clear all filters
    Given I have applied filters for price range 50.00 to 300.00
    And I have applied filter for category "Electronics"
    When I clear all filters
    Then I should see all 8 products

  Scenario: Filter by exact rating value
    When I filter products with minimum rating 4.5
    Then I should see 3 products
    And the products should include "Laptop", "Coffee Maker", "Programming Book"

  Scenario: Filter by price range with no minimum
    When I filter products with maximum price 50.00
    Then I should see 3 products
    And the products should include "Garden Hose", "Programming Book", "Fiction Novel"

  Scenario: Filter by price range with no maximum
    When I filter products with minimum price 200.00
    Then I should see 2 products
    And the products should include "Laptop", "Smartphone"

  # Recently Viewed Products Scenarios
  Scenario: Track recently viewed products during session
    When I view product "Laptop" details
    And I view product "Smartphone" details
    And I view product "Coffee Maker" details
    Then I should have 3 recently viewed products
    And the recently viewed products should include "Laptop", "Smartphone", "Coffee Maker"
    And the most recently viewed product should be "Coffee Maker"

  Scenario: Recently viewed products do not contain duplicates
    When I view product "Laptop" details
    And I view product "Smartphone" details
    And I view product "Laptop" details again
    Then I should have 2 recently viewed products
    And the recently viewed products should include "Laptop", "Smartphone"
    And the most recently viewed product should be "Laptop"

  Scenario: Recently viewed products maintain order
    When I view product "Tablet" details
    And I view product "Garden Hose" details
    And I view product "Programming Book" details
    And I view product "Smartphone" details
    Then I should have 4 recently viewed products
    And the recently viewed products should be in order "Smartphone", "Programming Book", "Garden Hose", "Tablet"

  Scenario: Display recently viewed products with product information
    When I view product "Laptop" details
    And I view product "Coffee Maker" details
    Then I should have 2 recently viewed products
    And the recently viewed product "Laptop" should display name "Laptop", price 799.99, and category "Electronics"
    And the recently viewed product "Coffee Maker" should display name "Coffee Maker", price 89.99, and category "Home Goods"

  Scenario: Recently viewed products persist during session
    Given I have viewed products "Laptop" and "Smartphone" in my session
    When I navigate to different pages within the application
    And I return to the product listing page
    Then I should still have 2 recently viewed products
    And the recently viewed products should include "Laptop", "Smartphone"

  Scenario: Clear recently viewed products when session ends
    Given I have viewed products "Laptop", "Smartphone", and "Coffee Maker" in my session
    When I end my session
    And I start a new session
    Then I should have 0 recently viewed products

  Scenario: Clicking recently viewed product navigates to detail page
    Given I have viewed products "Tablet" and "Programming Book" in my session
    When I click on recently viewed product "Tablet"
    Then I should be navigated to the product detail page for "Tablet"

  Scenario: Recently viewed products limit to reasonable number
    When I view products "Laptop", "Smartphone", "Coffee Maker", "Garden Hose", "Programming Book", "Fiction Novel", "Tablet", "Blender", and 5 more products
    Then I should have at most 10 recently viewed products
    And the oldest viewed products should be removed from the list

  # Combined Filtering and Recently Viewed Scenarios
  Scenario: Filter products while having recently viewed products
    Given I have viewed products "Laptop" and "Coffee Maker" in my session
    When I filter products by category "Electronics"
    Then I should see 3 products from the Electronics category
    And I should still have 2 recently viewed products
    And the recently viewed products should include "Laptop", "Coffee Maker"

  Scenario: Add to recently viewed while filters are applied
    Given I have applied filter for category "Electronics"
    When I view product "Smartphone" details from the filtered results
    Then I should have 1 recently viewed product
    And the recently viewed products should include "Smartphone"
    And the filtered results should still show only Electronics products 