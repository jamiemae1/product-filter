package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Category;
import com.mycompany.myapp.repository.CategoryRepository;
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
 * Unit tests for {@link CategoryService}.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic devices and accessories");
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("should save category successfully")
        void shouldSaveCategorySuccessfully() {
            // Arrange
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Category result = categoryService.save(testCategory);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Electronics");
            assertThat(result.getDescription()).isEqualTo("Electronic devices and accessories");
            verify(categoryRepository, times(1)).save(testCategory);
        }

        @Test
        @DisplayName("should save category with null description successfully")
        void shouldSaveCategoryWithNullDescriptionSuccessfully() {
            // Arrange
            Category categoryWithoutDescription = new Category();
            categoryWithoutDescription.setId(2L);
            categoryWithoutDescription.setName("Books");
            categoryWithoutDescription.setDescription(null);

            when(categoryRepository.save(any(Category.class))).thenReturn(categoryWithoutDescription);

            // Act
            Category result = categoryService.save(categoryWithoutDescription);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getName()).isEqualTo("Books");
            assertThat(result.getDescription()).isNull();
            verify(categoryRepository, times(1)).save(categoryWithoutDescription);
        }

        @Test
        @DisplayName("should update category successfully")
        void shouldUpdateCategorySuccessfully() {
            // Arrange
            Category updatedCategory = new Category();
            updatedCategory.setId(1L);
            updatedCategory.setName("Updated Electronics");
            updatedCategory.setDescription("Updated description");
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            // Act
            Category result = categoryService.update(updatedCategory);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Electronics");
            assertThat(result.getDescription()).isEqualTo("Updated description");
            verify(categoryRepository, times(1)).save(updatedCategory);
        }

        @Test
        @DisplayName("should partially update existing category successfully")
        void shouldPartiallyUpdateExistingCategorySuccessfully() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(1L);
            partialCategory.setName("Updated Name");
            partialCategory.setDescription("Updated Description");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Updated Name");
            assertThat(result.get().getDescription()).isEqualTo("Updated Description");
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("should partially update category with only name")
        void shouldPartiallyUpdateCategoryWithOnlyName() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(1L);
            partialCategory.setName("New Name Only");
            // description is null

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("New Name Only");
            // Original description should remain unchanged since partial update had null description
            assertThat(result.get().getDescription()).isEqualTo("Electronic devices and accessories");
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("should partially update category with only description")
        void shouldPartiallyUpdateCategoryWithOnlyDescription() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(1L);
            partialCategory.setDescription("New Description Only");
            // name is null

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isEqualTo("New Description Only");
            // Original name should remain unchanged since partial update had null name
            assertThat(result.get().getName()).isEqualTo("Electronics");
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("should return empty optional when partially updating non-existent category")
        void shouldReturnEmptyOptionalWhenPartiallyUpdatingNonExistentCategory() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(999L);
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isEmpty();
            verify(categoryRepository, times(1)).findById(999L);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("should find category by id successfully")
        void shouldFindCategoryByIdSuccessfully() {
            // Arrange
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

            // Act
            Optional<Category> result = categoryService.findOne(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getName()).isEqualTo("Electronics");
            assertThat(result.get().getDescription()).isEqualTo("Electronic devices and accessories");
            verify(categoryRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should return empty optional when category not found")
        void shouldReturnEmptyOptionalWhenCategoryNotFound() {
            // Arrange
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Category> result = categoryService.findOne(999L);

            // Assert
            assertThat(result).isEmpty();
            verify(categoryRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("should delete category by id successfully")
        void shouldDeleteCategoryByIdSuccessfully() {
            // Act
            categoryService.delete(1L);

            // Assert
            verify(categoryRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should find all categories with pagination")
        void shouldFindAllCategoriesWithPagination() {
            // Arrange
            Category category2 = new Category();
            category2.setId(2L);
            category2.setName("Books");
            category2.setDescription("Books and literature");

            Pageable pageable = PageRequest.of(0, 10);
            List<Category> categories = Arrays.asList(testCategory, category2);
            Page<Category> categoryPage = new PageImpl<>(categories, pageable, 2);
            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

            // Act
            Page<Category> result = categoryService.findAll(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Electronics");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Books");
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(categoryRepository, times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("should find all categories with empty page")
        void shouldFindAllCategoriesWithEmptyPage() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
            when(categoryRepository.findAll(pageable)).thenReturn(emptyPage);

            // Act
            Page<Category> result = categoryService.findAll(pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(categoryRepository, times(1)).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("should handle null category in save operation")
        void shouldHandleNullCategoryInSaveOperation() {
            // Arrange
            when(categoryRepository.save(null)).thenReturn(null);

            // Act
            Category result = categoryService.save(null);

            // Assert
            assertThat(result).isNull();
            verify(categoryRepository, times(1)).save(null);
        }

        @Test
        @DisplayName("should handle null category in update operation")
        void shouldHandleNullCategoryInUpdateOperation() {
            // Arrange
            when(categoryRepository.save(null)).thenReturn(null);

            // Act
            Category result = categoryService.update(null);

            // Assert
            assertThat(result).isNull();
            verify(categoryRepository, times(1)).save(null);
        }

        @Test
        @DisplayName("should handle partial update with all null fields gracefully")
        void shouldHandlePartialUpdateWithAllNullFieldsGracefully() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(1L);
            // Both name and description are null

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isPresent();
            // Original values should remain unchanged since partial update fields were null
            assertThat(result.get().getName()).isEqualTo("Electronics");
            assertThat(result.get().getDescription()).isEqualTo("Electronic devices and accessories");
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        @DisplayName("should handle delete with null id gracefully")
        void shouldHandleDeleteWithNullIdGracefully() {
            // Act & Assert - This should not throw an exception
            categoryService.delete(null);
            verify(categoryRepository, times(1)).deleteById(null);
        }

        @Test
        @DisplayName("should handle find by null id gracefully")
        void shouldHandleFindByNullIdGracefully() {
            // Arrange
            when(categoryRepository.findById(null)).thenReturn(Optional.empty());

            // Act
            Optional<Category> result = categoryService.findOne(null);

            // Assert
            assertThat(result).isEmpty();
            verify(categoryRepository, times(1)).findById(null);
        }

        @Test
        @DisplayName("should handle large page size in findAll")
        void shouldHandleLargePageSizeInFindAll() {
            // Arrange
            Pageable largePageable = PageRequest.of(0, 1000);
            Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory), largePageable, 1);
            when(categoryRepository.findAll(largePageable)).thenReturn(categoryPage);

            // Act
            Page<Category> result = categoryService.findAll(largePageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(categoryRepository, times(1)).findAll(largePageable);
        }

        @Test
        @DisplayName("should handle category with maximum name length")
        void shouldHandleCategoryWithMaximumNameLength() {
            // Arrange - Name has max length of 50 characters
            String maxLengthName = "A".repeat(50);
            Category maxNameCategory = new Category();
            maxNameCategory.setId(3L);
            maxNameCategory.setName(maxLengthName);
            maxNameCategory.setDescription("Test description");

            when(categoryRepository.save(any(Category.class))).thenReturn(maxNameCategory);

            // Act
            Category result = categoryService.save(maxNameCategory);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).hasSize(50);
            assertThat(result.getName()).isEqualTo(maxLengthName);
            verify(categoryRepository, times(1)).save(maxNameCategory);
        }

        @Test
        @DisplayName("should handle category with maximum description length")
        void shouldHandleCategoryWithMaximumDescriptionLength() {
            // Arrange - Description has max length of 255 characters
            String maxLengthDescription = "D".repeat(255);
            Category maxDescriptionCategory = new Category();
            maxDescriptionCategory.setId(4L);
            maxDescriptionCategory.setName("Test Category");
            maxDescriptionCategory.setDescription(maxLengthDescription);

            when(categoryRepository.save(any(Category.class))).thenReturn(maxDescriptionCategory);

            // Act
            Category result = categoryService.save(maxDescriptionCategory);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).hasSize(255);
            assertThat(result.getDescription()).isEqualTo(maxLengthDescription);
            verify(categoryRepository, times(1)).save(maxDescriptionCategory);
        }
    }

    @Nested
    @DisplayName("Business Logic Validation")
    class BusinessLogicValidation {

        @Test
        @DisplayName("should maintain category immutability during partial update")
        void shouldMaintainCategoryImmutabilityDuringPartialUpdate() {
            // Arrange
            Category originalCategory = new Category();
            originalCategory.setId(1L);
            originalCategory.setName("Original Name");
            originalCategory.setDescription("Original Description");

            Category partialUpdate = new Category();
            partialUpdate.setId(1L);
            partialUpdate.setName("Updated Name");
            // Description is null, should not change original

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(originalCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(originalCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialUpdate);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Updated Name");
            assertThat(result.get().getDescription()).isEqualTo("Original Description");
        }

        @Test
        @DisplayName("should preserve category ID during updates")
        void shouldPreserveCategoryIdDuringUpdates() {
            // Arrange
            Category updateCategory = new Category();
            updateCategory.setId(1L);
            updateCategory.setName("Updated Category");
            updateCategory.setDescription("Updated Description");

            when(categoryRepository.save(any(Category.class))).thenReturn(updateCategory);

            // Act
            Category result = categoryService.update(updateCategory);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(categoryRepository, times(1)).save(updateCategory);
        }

        @Test
        @DisplayName("should handle empty string values in partial update")
        void shouldHandleEmptyStringValuesInPartialUpdate() {
            // Arrange
            Category partialCategory = new Category();
            partialCategory.setId(1L);
            partialCategory.setName(""); // Empty string
            partialCategory.setDescription(""); // Empty string

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

            // Act
            Optional<Category> result = categoryService.partialUpdate(partialCategory);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEmpty();
            assertThat(result.get().getDescription()).isEmpty();
            verify(categoryRepository, times(1)).findById(1L);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }
    }
}
