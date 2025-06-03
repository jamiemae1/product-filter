package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Product}.
 */
@Service
@Transactional
public class ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

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

    // Product filtering methods for Cucumber scenarios

    /**
     * Find products by price range.
     *
     * @param minPrice the minimum price.
     * @param maxPrice the maximum price.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        LOG.debug("Request to find Products by price range : {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Find products by category name.
     *
     * @param categoryName the category name.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByCategoryName(String categoryName) {
        LOG.debug("Request to find Products by category name : {}", categoryName);
        return productRepository.findByCategoryName(categoryName);
    }

    /**
     * Find products by multiple category names.
     *
     * @param categoryNames the list of category names.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByCategoryNames(List<String> categoryNames) {
        LOG.debug("Request to find Products by category names : {}", categoryNames);
        return productRepository.findByCategoryNames(categoryNames);
    }

    /**
     * Find products by minimum rating.
     *
     * @param minRating the minimum rating.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByMinimumRating(Double minRating) {
        LOG.debug("Request to find Products by minimum rating : {}", minRating);
        return productRepository.findByRatingGreaterThanEqual(minRating);
    }

    /**
     * Find products by maximum price.
     *
     * @param maxPrice the maximum price.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByMaxPrice(BigDecimal maxPrice) {
        LOG.debug("Request to find Products by maximum price : {}", maxPrice);
        return productRepository.findByPriceLessThanEqual(maxPrice);
    }

    /**
     * Find products by minimum price.
     *
     * @param minPrice the minimum price.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByMinPrice(BigDecimal minPrice) {
        LOG.debug("Request to find Products by minimum price : {}", minPrice);
        return productRepository.findByPriceGreaterThanEqual(minPrice);
    }

    /**
     * Find products by price range and category.
     *
     * @param minPrice the minimum price.
     * @param maxPrice the maximum price.
     * @param categoryName the category name.
     * @return the list of products.
     */
    @Transactional(readOnly = true)
    public List<Product> findByPriceRangeAndCategory(BigDecimal minPrice, BigDecimal maxPrice, String categoryName) {
        LOG.debug("Request to find Products by price range and category : {} - {} in {}", minPrice, maxPrice, categoryName);
        return productRepository.findByPriceBetweenAndCategoryName(minPrice, maxPrice, categoryName);
    }

    /**
     * Find all products without pagination.
     *
     * @return the list of all products.
     */
    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        LOG.debug("Request to find all Products");
        return productRepository.findAllWithToOneRelationships();
    }
}
