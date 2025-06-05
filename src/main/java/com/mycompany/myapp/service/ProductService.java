package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Product}.
 */
@Service
@Transactional
@SessionScope
public class ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);
    private static final int MAX_RECENTLY_VIEWED = 10;

    private final ProductRepository productRepository;
    private final Map<String, Product> recentlyViewedProducts = new LinkedHashMap<>();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Save a product.
     *
     * @param product the entity to save.
     * @return the persisted entity.
     */
    public Product save(Product product) {
        LOG.debug("Request to save Product : {}", product);
        return productRepository.save(product);
    }

    /**
     * Update a product.
     *
     * @param product the entity to save.
     * @return the persisted entity.
     */
    public Product update(Product product) {
        LOG.debug("Request to update Product : {}", product);
        return productRepository.save(product);
    }

    /**
     * Partially update a product.
     *
     * @param product the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Product> partialUpdate(Product product) {
        LOG.debug("Request to partially update Product : {}", product);

        return productRepository
            .findById(product.getId())
            .map(existingProduct -> {
                if (product.getName() != null) {
                    existingProduct.setName(product.getName());
                }
                if (product.getDescription() != null) {
                    existingProduct.setDescription(product.getDescription());
                }
                if (product.getPrice() != null) {
                    existingProduct.setPrice(product.getPrice());
                }
                if (product.getImageUrl() != null) {
                    existingProduct.setImageUrl(product.getImageUrl());
                }
                if (product.getRating() != null) {
                    existingProduct.setRating(product.getRating());
                }

                return existingProduct;
            })
            .map(productRepository::save);
    }

    /**
     * Get all the products.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        LOG.debug("Request to get all Products");
        return productRepository.findAll(pageable);
    }

    /**
     * Get all the products with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<Product> findAllWithEagerRelationships(Pageable pageable) {
        return productRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Product> findOne(Long id) {
        LOG.debug("Request to get Product : {}", id);
        return productRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Product : {}", id);
        productRepository.deleteById(id);
    }

    // Product Filtering Methods

    /**
     * Find products by price range.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of products within the price range
     */
    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        LOG.debug("Request to find products by price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Find products by minimum price.
     *
     * @param minPrice minimum price
     * @return list of products with price >= minPrice
     */
    @Transactional(readOnly = true)
    public List<Product> findByMinPrice(BigDecimal minPrice) {
        LOG.debug("Request to find products by minimum price: {}", minPrice);
        return productRepository.findByMinPrice(minPrice);
    }

    /**
     * Find products by maximum price.
     *
     * @param maxPrice maximum price
     * @return list of products with price <= maxPrice
     */
    @Transactional(readOnly = true)
    public List<Product> findByMaxPrice(BigDecimal maxPrice) {
        LOG.debug("Request to find products by maximum price: {}", maxPrice);
        return productRepository.findByMaxPrice(maxPrice);
    }

    /**
     * Find products by category name.
     *
     * @param categoryName category name
     * @return list of products in the specified category
     */
    @Transactional(readOnly = true)
    public List<Product> findByCategoryName(String categoryName) {
        LOG.debug("Request to find products by category: {}", categoryName);
        return productRepository.findByCategoryName(categoryName);
    }

    /**
     * Find products by multiple category names.
     *
     * @param categoryNames list of category names
     * @return list of products in any of the specified categories
     */
    @Transactional(readOnly = true)
    public List<Product> findByCategoryNames(List<String> categoryNames) {
        LOG.debug("Request to find products by categories: {}", categoryNames);
        return productRepository.findByCategoryNames(categoryNames);
    }

    /**
     * Find products by minimum rating.
     *
     * @param minRating minimum rating
     * @return list of products with rating >= minRating
     */
    @Transactional(readOnly = true)
    public List<Product> findByMinimumRating(Double minRating) {
        LOG.debug("Request to find products by minimum rating: {}", minRating);
        return productRepository.findByMinimumRating(minRating);
    }

    /**
     * Find products by price range and category.
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param categoryName category name
     * @return list of products matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Product> findByPriceRangeAndCategory(BigDecimal minPrice, BigDecimal maxPrice, String categoryName) {
        LOG.debug("Request to find products by price range {} - {} and category: {}", minPrice, maxPrice, categoryName);
        return productRepository.findByPriceRangeAndCategory(minPrice, maxPrice, categoryName);
    }

    /**
     * Find products by combined filters (all optional).
     *
     * @param minPrice minimum price (optional)
     * @param maxPrice maximum price (optional)
     * @param categoryName category name (optional)
     * @param minRating minimum rating (optional)
     * @return list of products matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Product> findByCombinedFilters(BigDecimal minPrice, BigDecimal maxPrice, String categoryName, Double minRating) {
        LOG.debug(
            "Request to find products by combined filters - Price: {}-{}, Category: {}, Rating: {}",
            minPrice,
            maxPrice,
            categoryName,
            minRating
        );
        return productRepository.findByCombinedFilters(minPrice, maxPrice, categoryName, minRating);
    }

    /**
     * Get all products without pagination.
     *
     * @return list of all products
     */
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        LOG.debug("Request to get all products without pagination");
        return productRepository.findAllWithToOneRelationships();
    }

    // Recently Viewed Products Methods

    /**
     * Track a product as recently viewed.
     *
     * @param productName the name of the product viewed
     */
    public void viewProductDetails(String productName) {
        LOG.debug("Request to track recently viewed product: {}", productName);

        // Find the product by name
        Optional<Product> productOpt = productRepository
            .findAllWithToOneRelationships()
            .stream()
            .filter(p -> p.getName().equals(productName))
            .findFirst();

        if (productOpt.isPresent()) {
            Product product = productOpt.orElseThrow();

            // Remove if already exists (to update position)
            recentlyViewedProducts.remove(product.getName());

            // Add to the beginning (most recent)
            recentlyViewedProducts.put(product.getName(), product);

            // Maintain max size by removing oldest entries
            if (recentlyViewedProducts.size() > MAX_RECENTLY_VIEWED) {
                String oldestKey = recentlyViewedProducts.keySet().iterator().next();
                recentlyViewedProducts.remove(oldestKey);
            }

            LOG.debug("Product {} added to recently viewed. Total recently viewed: {}", productName, recentlyViewedProducts.size());
        } else {
            LOG.warn("Product with name {} not found", productName);
        }
    }

    /**
     * Track multiple products as viewed (for testing scenarios).
     *
     * @param productNames list of product names viewed
     */
    public void viewMultipleProducts(List<String> productNames) {
        LOG.debug("Request to track multiple products: {}", productNames);
        for (String productName : productNames) {
            viewProductDetails(productName);
        }
    }

    /**
     * Get recently viewed products.
     *
     * @return list of recently viewed products in order (most recent first)
     */
    public List<Product> getRecentlyViewedProducts() {
        LOG.debug("Request to get recently viewed products");
        // Return in reverse order (most recent first)
        List<Product> products = new ArrayList<>(recentlyViewedProducts.values());
        Collections.reverse(products);
        return products;
    }

    /**
     * Simulate page navigation (for testing session persistence).
     */
    public void simulatePageNavigation() {
        LOG.debug("Simulating page navigation - recently viewed products should persist");
        // In a real application, this would be handled by session management
        // Here we just log that navigation occurred without affecting the recently viewed list
    }

    /**
     * End current session and clear recently viewed products.
     */
    public void endSession() {
        LOG.debug("Ending session - clearing recently viewed products");
        recentlyViewedProducts.clear();
    }

    /**
     * Start a new session.
     */
    public void startNewSession() {
        LOG.debug("Starting new session");
        recentlyViewedProducts.clear();
    }

    /**
     * Navigate to product detail page.
     *
     * @param productName the name of the product to navigate to
     * @return navigation target path
     */
    public String navigateToProductDetail(String productName) {
        LOG.debug("Request to navigate to product detail: {}", productName);

        // Find the product by name to get its ID
        Optional<Product> productOpt = productRepository
            .findAllWithToOneRelationships()
            .stream()
            .filter(p -> p.getName().equals(productName))
            .findFirst();

        if (productOpt.isPresent()) {
            Product product = productOpt.orElseThrow();
            String navigationPath = "/product/" + product.getId();
            LOG.debug("Navigation path for product {}: {}", productName, navigationPath);
            return navigationPath;
        } else {
            LOG.warn("Product with name {} not found for navigation", productName);
            return "/products";
        }
    }
}
