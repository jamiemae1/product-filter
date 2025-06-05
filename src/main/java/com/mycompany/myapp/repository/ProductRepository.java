package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select product from Product product where product.user.login = ?#{authentication.name}")
    List<Product> findByUserIsCurrentUser();

    // Product filtering query methods
    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price >= :minPrice and product.price <= :maxPrice"
    )
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price >= :minPrice"
    )
    List<Product> findByMinPrice(@Param("minPrice") BigDecimal minPrice);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price <= :maxPrice"
    )
    List<Product> findByMaxPrice(@Param("maxPrice") BigDecimal maxPrice);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.category.name = :categoryName"
    )
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.category.name in :categoryNames"
    )
    List<Product> findByCategoryNames(@Param("categoryNames") List<String> categoryNames);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.rating >= :minRating"
    )
    List<Product> findByMinimumRating(@Param("minRating") Double minRating);

    // Combined filtering methods
    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price >= :minPrice and product.price <= :maxPrice and product.category.name = :categoryName"
    )
    List<Product> findByPriceRangeAndCategory(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("categoryName") String categoryName
    );

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price >= :minPrice and product.price <= :maxPrice and product.rating >= :minRating"
    )
    List<Product> findByPriceRangeAndRating(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minRating") Double minRating
    );

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.category.name = :categoryName and product.rating >= :minRating"
    )
    List<Product> findByCategoryAndRating(@Param("categoryName") String categoryName, @Param("minRating") Double minRating);

    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where product.price >= :minPrice and product.price <= :maxPrice and product.category.name = :categoryName and product.rating >= :minRating"
    )
    List<Product> findByPriceRangeAndCategoryAndRating(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("categoryName") String categoryName,
        @Param("minRating") Double minRating
    );

    // Dynamic filtering method
    @Query(
        "select product from Product product left join fetch product.category left join fetch product.user where " +
        "(:minPrice is null or product.price >= :minPrice) and " +
        "(:maxPrice is null or product.price <= :maxPrice) and " +
        "(:categoryName is null or product.category.name = :categoryName) and " +
        "(:minRating is null or product.rating >= :minRating)"
    )
    List<Product> findByCombinedFilters(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("categoryName") String categoryName,
        @Param("minRating") Double minRating
    );

    default Optional<Product> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Product> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Product> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select product from Product product left join fetch product.category left join fetch product.user",
        countQuery = "select count(product) from Product product"
    )
    Page<Product> findAllWithToOneRelationships(Pageable pageable);

    @Query("select product from Product product left join fetch product.category left join fetch product.user")
    List<Product> findAllWithToOneRelationships();

    @Query("select product from Product product left join fetch product.category left join fetch product.user where product.id =:id")
    Optional<Product> findOneWithToOneRelationships(@Param("id") Long id);
}
