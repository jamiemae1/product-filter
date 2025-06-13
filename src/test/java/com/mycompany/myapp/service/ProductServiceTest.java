package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setImageUrl("http://example.com/image.jpg");
        testProduct.setRating(4.5);
        testProduct.setCategory(testCategory);
        testProduct.setUser(testUser);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("should save product successfully")
        void shouldSaveProductSuccessfully() {
            // Arrange
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            Product result = productService.save(testProduct);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Arrange
            Product updatedProduct = new Product();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Product");
            updatedProduct.setPrice(new BigDecimal("149.99"));
            when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

            // Act
            Product result = productService.update(updatedProduct);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Product");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("149.99"));
            verify(productRepository, times(1)).save(updatedProduct);
        }

        @Test
        @DisplayName("should partially update existing product successfully")
        void shouldPartiallyUpdateExistingProductSuccessfully() {
            // Arrange
            Product partialProduct = new Product();
            partialProduct.setId(1L);
            partialProduct.setName("Updated Name");
            partialProduct.setPrice(new BigDecimal("199.99"));

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            Optional<Product> result = productService.partialUpdate(partialProduct);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Updated Name");
            assertThat(result.get().getPrice()).isEqualTo(new BigDecimal("199.99"));
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should return empty optional when partially updating non-existent product")
        void shouldReturnEmptyOptionalWhenPartiallyUpdatingNonExistentProduct() {
            // Arrange
            Product partialProduct = new Product();
            partialProduct.setId(999L);
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Product> result = productService.partialUpdate(partialProduct);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("should find product by id successfully")
        void shouldFindProductByIdSuccessfully() {
            // Arrange
            when(productRepository.findOneWithEagerRelationships(1L)).thenReturn(Optional.of(testProduct));

            // Act
            Optional<Product> result = productService.findOne(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findOneWithEagerRelationships(1L);
        }

        @Test
        @DisplayName("should return empty optional when product not found")
        void shouldReturnEmptyOptionalWhenProductNotFound() {
            // Arrange
            when(productRepository.findOneWithEagerRelationships(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Product> result = productService.findOne(999L);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findOneWithEagerRelationships(999L);
        }

        @Test
        @DisplayName("should delete product by id successfully")
        void shouldDeleteProductByIdSuccessfully() {
            // Act
            productService.delete(1L);

            // Assert
            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should find all products with pagination")
        void shouldFindAllProductsWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            when(productRepository.findAll(pageable)).thenReturn(productPage);

            // Act
            Page<Product> result = productService.findAll(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("should find all products with eager relationships")
        void shouldFindAllProductsWithEagerRelationships() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(products, pageable, 1);
            when(productRepository.findAllWithEagerRelationships(pageable)).thenReturn(productPage);

            // Act
            Page<Product> result = productService.findAllWithEagerRelationships(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findAllWithEagerRelationships(pageable);
        }
    }

    @Nested
    @DisplayName("Price Filtering")
    class PriceFiltering {

        @Test
        @DisplayName("should find products by price range successfully")
        void shouldFindProductsByPriceRangeSuccessfully() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("50.00");
            BigDecimal maxPrice = new BigDecimal("150.00");
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(products);

            // Act
            List<Product> result = productService.findByPriceRange(minPrice, maxPrice);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByPriceRange(minPrice, maxPrice);
        }

        @Test
        @DisplayName("should find products by minimum price successfully")
        void shouldFindProductsByMinimumPriceSuccessfully() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("50.00");
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByMinPrice(minPrice)).thenReturn(products);

            // Act
            List<Product> result = productService.findByMinPrice(minPrice);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByMinPrice(minPrice);
        }

        @Test
        @DisplayName("should find products by maximum price successfully")
        void shouldFindProductsByMaximumPriceSuccessfully() {
            // Arrange
            BigDecimal maxPrice = new BigDecimal("150.00");
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByMaxPrice(maxPrice)).thenReturn(products);

            // Act
            List<Product> result = productService.findByMaxPrice(maxPrice);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByMaxPrice(maxPrice);
        }

        @Test
        @DisplayName("should return empty list when no products match price range")
        void shouldReturnEmptyListWhenNoProductsMatchPriceRange() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("200.00");
            BigDecimal maxPrice = new BigDecimal("300.00");
            when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(new ArrayList<>());

            // Act
            List<Product> result = productService.findByPriceRange(minPrice, maxPrice);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findByPriceRange(minPrice, maxPrice);
        }
    }

    @Nested
    @DisplayName("Category Filtering")
    class CategoryFiltering {

        @Test
        @DisplayName("should find products by category name successfully")
        void shouldFindProductsByCategoryNameSuccessfully() {
            // Arrange
            String categoryName = "Electronics";
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByCategoryName(categoryName)).thenReturn(products);

            // Act
            List<Product> result = productService.findByCategoryName(categoryName);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByCategoryName(categoryName);
        }

        @Test
        @DisplayName("should find products by multiple category names successfully")
        void shouldFindProductsByMultipleCategoryNamesSuccessfully() {
            // Arrange
            List<String> categoryNames = Arrays.asList("Electronics", "Computers");
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByCategoryNames(categoryNames)).thenReturn(products);

            // Act
            List<Product> result = productService.findByCategoryNames(categoryNames);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByCategoryNames(categoryNames);
        }

        @Test
        @DisplayName("should return empty list when no products match category")
        void shouldReturnEmptyListWhenNoProductsMatchCategory() {
            // Arrange
            String categoryName = "NonExistent";
            when(productRepository.findByCategoryName(categoryName)).thenReturn(new ArrayList<>());

            // Act
            List<Product> result = productService.findByCategoryName(categoryName);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findByCategoryName(categoryName);
        }
    }

    @Nested
    @DisplayName("Rating and Combined Filtering")
    class RatingAndCombinedFiltering {

        @Test
        @DisplayName("should find products by minimum rating successfully")
        void shouldFindProductsByMinimumRatingSuccessfully() {
            // Arrange
            Double minRating = 4.0;
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByMinimumRating(minRating)).thenReturn(products);

            // Act
            List<Product> result = productService.findByMinimumRating(minRating);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByMinimumRating(minRating);
        }

        @Test
        @DisplayName("should find products by price range and category successfully")
        void shouldFindProductsByPriceRangeAndCategorySuccessfully() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("50.00");
            BigDecimal maxPrice = new BigDecimal("150.00");
            String categoryName = "Electronics";
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByPriceRangeAndCategory(minPrice, maxPrice, categoryName)).thenReturn(products);

            // Act
            List<Product> result = productService.findByPriceRangeAndCategory(minPrice, maxPrice, categoryName);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByPriceRangeAndCategory(minPrice, maxPrice, categoryName);
        }

        @Test
        @DisplayName("should find products by combined filters successfully")
        void shouldFindProductsByCombinedFiltersSuccessfully() {
            // Arrange
            BigDecimal minPrice = new BigDecimal("50.00");
            BigDecimal maxPrice = new BigDecimal("150.00");
            String categoryName = "Electronics";
            Double minRating = 4.0;
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByCombinedFilters(minPrice, maxPrice, categoryName, minRating)).thenReturn(products);

            // Act
            List<Product> result = productService.findByCombinedFilters(minPrice, maxPrice, categoryName, minRating);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findByCombinedFilters(minPrice, maxPrice, categoryName, minRating);
        }

        @Test
        @DisplayName("should find products by combined filters with null values")
        void shouldFindProductsByCombinedFiltersWithNullValues() {
            // Arrange
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findByCombinedFilters(null, null, null, null)).thenReturn(products);

            // Act
            List<Product> result = productService.findByCombinedFilters(null, null, null, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findByCombinedFilters(null, null, null, null);
        }
    }

    @Nested
    @DisplayName("Recently Viewed Products")
    class RecentlyViewedProducts {

        @Test
        @DisplayName("should track product as recently viewed successfully")
        void shouldTrackProductAsRecentlyViewedSuccessfully() {
            // Arrange
            List<Product> allProducts = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);

            // Act
            productService.viewProductDetails("Test Product");

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).hasSize(1);
            assertThat(recentlyViewed.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should not track non-existent product as recently viewed")
        void shouldNotTrackNonExistentProductAsRecentlyViewed() {
            // Arrange
            when(productRepository.findAllWithToOneRelationships()).thenReturn(new ArrayList<>());

            // Act
            productService.viewProductDetails("Non-existent Product");

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).isEmpty();
            verify(productRepository, times(1)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should maintain maximum recently viewed products limit")
        void shouldMaintainMaximumRecentlyViewedProductsLimit() {
            // Arrange
            List<Product> allProducts = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                Product product = new Product();
                product.setId((long) i);
                product.setName("Product " + i);
                allProducts.add(product);
            }
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);

            // Act - View 15 products (exceeds MAX_RECENTLY_VIEWED = 10)
            for (int i = 1; i <= 15; i++) {
                productService.viewProductDetails("Product " + i);
            }

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).hasSize(10); // Should be limited to MAX_RECENTLY_VIEWED
            assertThat(recentlyViewed.get(0).getName()).isEqualTo("Product 15"); // Most recent first
            assertThat(recentlyViewed.get(9).getName()).isEqualTo("Product 6"); // Oldest kept
        }

        @Test
        @DisplayName("should update position when viewing same product again")
        void shouldUpdatePositionWhenViewingSameProductAgain() {
            // Arrange
            Product product1 = new Product();
            product1.setId(1L);
            product1.setName("Product 1");

            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Product 2");

            List<Product> allProducts = Arrays.asList(product1, product2);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);

            // Act
            productService.viewProductDetails("Product 1");
            productService.viewProductDetails("Product 2");
            productService.viewProductDetails("Product 1"); // View Product 1 again

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).hasSize(2);
            assertThat(recentlyViewed.get(0).getName()).isEqualTo("Product 1"); // Should be most recent
            assertThat(recentlyViewed.get(1).getName()).isEqualTo("Product 2");
        }

        @Test
        @DisplayName("should track multiple products successfully")
        void shouldTrackMultipleProductsSuccessfully() {
            // Arrange
            Product product1 = new Product();
            product1.setId(1L);
            product1.setName("Product 1");

            Product product2 = new Product();
            product2.setId(2L);
            product2.setName("Product 2");

            List<Product> allProducts = Arrays.asList(product1, product2);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);
            List<String> productNames = Arrays.asList("Product 1", "Product 2");

            // Act
            productService.viewMultipleProducts(productNames);

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).hasSize(2);
            verify(productRepository, times(2)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should clear recently viewed products when ending session")
        void shouldClearRecentlyViewedProductsWhenEndingSession() {
            // Arrange
            List<Product> allProducts = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);
            productService.viewProductDetails("Test Product");

            // Act
            productService.endSession();

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).isEmpty();
        }

        @Test
        @DisplayName("should clear recently viewed products when starting new session")
        void shouldClearRecentlyViewedProductsWhenStartingNewSession() {
            // Arrange
            List<Product> allProducts = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);
            productService.viewProductDetails("Test Product");

            // Act
            productService.startNewSession();

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for recently viewed products initially")
        void shouldReturnEmptyListForRecentlyViewedProductsInitially() {
            // Act
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();

            // Assert
            assertThat(recentlyViewed).isEmpty();
        }
    }

    @Nested
    @DisplayName("Navigation and Utility Methods")
    class NavigationAndUtilityMethods {

        @Test
        @DisplayName("should navigate to product detail successfully")
        void shouldNavigateToProductDetailSuccessfully() {
            // Arrange
            List<Product> allProducts = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);

            // Act
            String navigationPath = productService.navigateToProductDetail("Test Product");

            // Assert
            assertThat(navigationPath).isEqualTo("/product/1");
            verify(productRepository, times(1)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should return default path when product not found for navigation")
        void shouldReturnDefaultPathWhenProductNotFoundForNavigation() {
            // Arrange
            when(productRepository.findAllWithToOneRelationships()).thenReturn(new ArrayList<>());

            // Act
            String navigationPath = productService.navigateToProductDetail("Non-existent Product");

            // Assert
            assertThat(navigationPath).isEqualTo("/products");
            verify(productRepository, times(1)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should find all products without pagination successfully")
        void shouldFindAllProductsWithoutPaginationSuccessfully() {
            // Arrange
            List<Product> products = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(products);

            // Act
            List<Product> result = productService.findAllProducts();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Product");
            verify(productRepository, times(1)).findAllWithToOneRelationships();
        }

        @Test
        @DisplayName("should simulate page navigation without affecting recently viewed products")
        void shouldSimulatePageNavigationWithoutAffectingRecentlyViewedProducts() {
            // Arrange
            List<Product> allProducts = Arrays.asList(testProduct);
            when(productRepository.findAllWithToOneRelationships()).thenReturn(allProducts);
            productService.viewProductDetails("Test Product");

            // Act
            productService.simulatePageNavigation();

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).hasSize(1); // Should remain unchanged
            assertThat(recentlyViewed.get(0).getName()).isEqualTo("Test Product");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("should handle null product in save operation")
        void shouldHandleNullProductInSaveOperation() {
            // Arrange
            when(productRepository.save(null)).thenReturn(null);

            // Act
            Product result = productService.save(null);

            // Assert
            assertThat(result).isNull();
            verify(productRepository, times(1)).save(null);
        }

        @Test
        @DisplayName("should handle partial update with null fields gracefully")
        void shouldHandlePartialUpdateWithNullFieldsGracefully() {
            // Arrange
            Product partialProduct = new Product();
            partialProduct.setId(1L);
            // All other fields are null

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            Optional<Product> result = productService.partialUpdate(partialProduct);

            // Assert
            assertThat(result).isPresent();
            // Original values should remain unchanged since partial update fields were null
            assertThat(result.get().getName()).isEqualTo("Test Product");
            assertThat(result.get().getDescription()).isEqualTo("Test Description");
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("should handle empty category names list")
        void shouldHandleEmptyCategoryNamesList() {
            // Arrange
            List<String> emptyCategoryNames = new ArrayList<>();
            when(productRepository.findByCategoryNames(emptyCategoryNames)).thenReturn(new ArrayList<>());

            // Act
            List<Product> result = productService.findByCategoryNames(emptyCategoryNames);

            // Assert
            assertThat(result).isEmpty();
            verify(productRepository, times(1)).findByCategoryNames(emptyCategoryNames);
        }

        @Test
        @DisplayName("should handle empty product names list for multiple view tracking")
        void shouldHandleEmptyProductNamesListForMultipleViewTracking() {
            // Arrange
            List<String> emptyProductNames = new ArrayList<>();

            // Act
            productService.viewMultipleProducts(emptyProductNames);

            // Assert
            List<Product> recentlyViewed = productService.getRecentlyViewedProducts();
            assertThat(recentlyViewed).isEmpty();
            verify(productRepository, never()).findAllWithToOneRelationships();
        }
    }
}
