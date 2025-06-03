Feature: Product Filtering
  As an online shopper
  I want to filter products by price range, category, and customer ratings
  So that I can quickly find products that match my preferences and budget

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

  Scenario: Filter products by price range
    When I filter products with minimum price 50.00 and maximum price 300.00
    Then I should see 4 products
    And the products should include "Smartphone", "Programming Book", "Tablet", "Blender"

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
    Then I should see 3 products
    And the products should include "Laptop", "Smartphone", "Tablet" 