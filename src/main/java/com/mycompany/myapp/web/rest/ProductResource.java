package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.repository.ProductRepository;
import com.mycompany.myapp.service.ProductService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Product}.
 */
@RestController
@RequestMapping("/api/products")
public class ProductResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProductResource.class);

    private static final String ENTITY_NAME = "product";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductService productService;

    private final ProductRepository productRepository;

    public ProductResource(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    /**
     * {@code POST  /products} : Create a new product.
     *
     * @param product the product to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new product, or with status {@code 400 (Bad Request)} if the product has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) throws URISyntaxException {
        LOG.debug("REST request to save Product : {}", product);
        if (product.getId() != null) {
            throw new BadRequestAlertException("A new product cannot already have an ID", ENTITY_NAME, "idexists");
        }
        product = productService.save(product);
        return ResponseEntity.created(new URI("/api/products/" + product.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, product.getId().toString()))
            .body(product);
    }

    /**
     * {@code PUT  /products/:id} : Updates an existing product.
     *
     * @param id the id of the product to save.
     * @param product the product to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated product,
     * or with status {@code 400 (Bad Request)} if the product is not valid,
     * or with status {@code 500 (Internal Server Error)} if the product couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Product product
    ) throws URISyntaxException {
        LOG.debug("REST request to update Product : {}, {}", id, product);
        if (product.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, product.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        product = productService.update(product);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, product.getId().toString()))
            .body(product);
    }

    /**
     * {@code PATCH  /products/:id} : Partial updates given fields of an existing product, field will ignore if it is null
     *
     * @param id the id of the product to save.
     * @param product the product to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated product,
     * or with status {@code 400 (Bad Request)} if the product is not valid,
     * or with status {@code 404 (Not Found)} if the product is not found,
     * or with status {@code 500 (Internal Server Error)} if the product couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Product> partialUpdateProduct(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Product product
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Product partially : {}, {}", id, product);
        if (product.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, product.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Product> result = productService.partialUpdate(product);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, product.getId().toString())
        );
    }

    /**
     * {@code GET  /products} : get all the products.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Product>> getAllProducts(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Products");
        Page<Product> page;
        if (eagerload) {
            page = productService.findAllWithEagerRelationships(pageable);
        } else {
            page = productService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /products/:id} : get the "id" product.
     *
     * @param id the id of the product to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the product, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Product : {}", id);
        Optional<Product> product = productService.findOne(id);
        return ResponseUtil.wrapOrNotFound(product);
    }

    /**
     * {@code DELETE  /products/:id} : delete the "id" product.
     *
     * @param id the id of the product to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Product : {}", id);
        productService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    // Product Filtering Endpoints

    /**
     * {@code GET  /products/filter/combined} : get products by combined filters.
     *
     * @param minPrice minimum price filter (optional).
     * @param maxPrice maximum price filter (optional).
     * @param categoryName category name filter (optional).
     * @param minRating minimum rating filter (optional).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of filtered products in body.
     */
    @GetMapping("/filter/combined")
    public ResponseEntity<List<Product>> getProductsByCombinedFilters(
        @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(name = "categoryName", required = false) String categoryName,
        @RequestParam(name = "minRating", required = false) Double minRating
    ) {
        LOG.debug(
            "REST request to get Products by combined filters - Price: {}-{}, Category: {}, Rating: {}",
            minPrice,
            maxPrice,
            categoryName,
            minRating
        );

        List<Product> products = productService.findByCombinedFilters(minPrice, maxPrice, categoryName, minRating);
        return ResponseEntity.ok().body(products);
    }

    /**
     * {@code GET  /products/filter/price} : get products by price range.
     *
     * @param minPrice minimum price filter.
     * @param maxPrice maximum price filter.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of filtered products in body.
     */
    @GetMapping("/filter/price")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
        @RequestParam("minPrice") BigDecimal minPrice,
        @RequestParam("maxPrice") BigDecimal maxPrice
    ) {
        LOG.debug("REST request to get Products by price range: {} - {}", minPrice, maxPrice);
        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok().body(products);
    }

    /**
     * {@code GET  /products/filter/category} : get products by category.
     *
     * @param categoryName category name filter.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of filtered products in body.
     */
    @GetMapping("/filter/category")
    public ResponseEntity<List<Product>> getProductsByCategory(@RequestParam("categoryName") String categoryName) {
        LOG.debug("REST request to get Products by category: {}", categoryName);
        List<Product> products = productService.findByCategoryName(categoryName);
        return ResponseEntity.ok().body(products);
    }

    /**
     * {@code GET  /products/filter/rating} : get products by minimum rating.
     *
     * @param minRating minimum rating filter.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of filtered products in body.
     */
    @GetMapping("/filter/rating")
    public ResponseEntity<List<Product>> getProductsByRating(@RequestParam("minRating") Double minRating) {
        LOG.debug("REST request to get Products by minimum rating: {}", minRating);
        List<Product> products = productService.findByMinimumRating(minRating);
        return ResponseEntity.ok().body(products);
    }

    // Recently Viewed Products Endpoints

    /**
     * {@code POST  /products/recently-viewed} : track a product as recently viewed.
     *
     * @param productName the name of the product viewed.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PostMapping("/recently-viewed")
    public ResponseEntity<Void> trackRecentlyViewedProduct(@RequestParam("productName") String productName) {
        LOG.debug("REST request to track recently viewed product: {}", productName);
        productService.viewProductDetails(productName);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /products/recently-viewed} : get recently viewed products.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recently viewed products in body.
     */
    @GetMapping("/recently-viewed")
    public ResponseEntity<List<Product>> getRecentlyViewedProducts() {
        LOG.debug("REST request to get recently viewed products");
        List<Product> products = productService.getRecentlyViewedProducts();
        return ResponseEntity.ok().body(products);
    }

    /**
     * {@code DELETE  /products/recently-viewed} : clear recently viewed products.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @DeleteMapping("/recently-viewed")
    public ResponseEntity<Void> clearRecentlyViewedProducts() {
        LOG.debug("REST request to clear recently viewed products");
        productService.endSession();
        return ResponseEntity.ok().build();
    }
}
