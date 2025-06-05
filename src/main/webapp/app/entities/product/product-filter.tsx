import React, { useEffect, useState } from 'react';
import { Button, Card, CardBody, CardHeader, Col, FormGroup, Input, Label, Row, Badge, Alert, Spinner, Container } from 'reactstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar, faFilter, faTimesCircle, faSearch, faEye, faPlus, faPencilAlt, faTrash } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';
import { RecentlyViewed } from './recently-viewed';

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl?: string;
  rating: number | null;
  category: {
    id: number;
    name: string;
    description: string;
  };
  user: {
    id: number;
    login: string;
  };
}

interface Category {
  id: number;
  name: string;
  description: string;
}

interface FilterState {
  minPrice: string;
  maxPrice: string;
  categoryName: string;
  minRating: string;
}

export const ProductFilter = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [noResults, setNoResults] = useState(false);
  const [recentlyViewedRefresh, setRecentlyViewedRefresh] = useState(0);
  const [filters, setFilters] = useState<FilterState>({
    minPrice: '',
    maxPrice: '',
    categoryName: '',
    minRating: '',
  });

  // Load all products and categories on component mount
  useEffect(() => {
    loadAllProducts();
    loadCategories();
  }, []);

  const loadAllProducts = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/products?eagerload=true&size=1000');
      setProducts(response.data);
      setNoResults(response.data.length === 0);
    } catch (error) {
      console.error('Error loading products:', error);
      setProducts([]);
      setNoResults(true);
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const response = await axios.get('/api/categories?size=1000');
      setCategories(response.data);
    } catch (error) {
      console.error('Error loading categories:', error);
      setCategories([]);
    }
  };

  const applyFilters = async () => {
    setLoading(true);
    setNoResults(false);

    try {
      const params = new URLSearchParams();

      if (filters.minPrice) params.append('minPrice', filters.minPrice);
      if (filters.maxPrice) params.append('maxPrice', filters.maxPrice);
      if (filters.categoryName) params.append('categoryName', filters.categoryName);
      if (filters.minRating) params.append('minRating', filters.minRating);

      const response = await axios.get(`/api/products/filter/combined?${params.toString()}`);
      setProducts(response.data);
      setNoResults(response.data.length === 0);
    } catch (error) {
      console.error('Error applying filters:', error);
      setProducts([]);
      setNoResults(true);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setFilters({
      minPrice: '',
      maxPrice: '',
      categoryName: '',
      minRating: '',
    });
    loadAllProducts();
  };

  const handleFilterChange = (field: keyof FilterState, value: string) => {
    setFilters(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const trackProductView = async (productName: string) => {
    try {
      await axios.post('/api/products/recently-viewed', null, {
        params: { productName },
      });
      // Trigger refresh of recently viewed component
      setRecentlyViewedRefresh(prev => prev + 1);
    } catch (error) {
      console.error('Error tracking product view:', error);
    }
  };

  const renderStars = (rating: number | null) => {
    if (rating == null) {
      // Return 5 empty stars for null/undefined ratings
      const stars = [];
      for (let i = 0; i < 5; i++) {
        stars.push(<FontAwesomeIcon key={`empty-${i}`} icon={faStar} className="text-muted" />);
      }
      return stars;
    }

    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(<FontAwesomeIcon key={i} icon={faStar} className="text-warning" />);
    }

    if (hasHalfStar) {
      stars.push(<FontAwesomeIcon key="half" icon={faStar} className="text-warning opacity-50" />);
    }

    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(<FontAwesomeIcon key={`empty-${i}`} icon={faStar} className="text-muted" />);
    }

    return stars;
  };

  const getActiveFiltersCount = () => {
    return Object.values(filters).filter(value => value !== '').length;
  };

  return (
    <Container fluid className="py-4">
      {/* Recently Viewed Products Section */}
      <Row className="mb-4">
        <Col xs={12}>
          <RecentlyViewed refreshTrigger={recentlyViewedRefresh} />
        </Col>
      </Row>

      <Row>
        {/* Filter Sidebar */}
        <Col lg={3} md={4} className="mb-4">
          <Card className="shadow-sm">
            <CardHeader className="bg-primary text-white">
              <FontAwesomeIcon icon={faFilter} className="me-2" />
              Product Filters
              {getActiveFiltersCount() > 0 && (
                <Badge color="light" className="ms-2 text-primary">
                  {getActiveFiltersCount()}
                </Badge>
              )}
            </CardHeader>
            <CardBody>
              {/* Price Range Filter */}
              <FormGroup>
                <Label className="fw-bold">Price Range</Label>
                <Row>
                  <Col xs={6}>
                    <Input
                      type="number"
                      placeholder="Min"
                      value={filters.minPrice}
                      onChange={e => handleFilterChange('minPrice', e.target.value)}
                      data-cy="minPriceInput"
                    />
                  </Col>
                  <Col xs={6}>
                    <Input
                      type="number"
                      placeholder="Max"
                      value={filters.maxPrice}
                      onChange={e => handleFilterChange('maxPrice', e.target.value)}
                      data-cy="maxPriceInput"
                    />
                  </Col>
                </Row>
              </FormGroup>

              {/* Category Filter */}
              <FormGroup>
                <Label className="fw-bold">Category</Label>
                <Input
                  type="select"
                  value={filters.categoryName}
                  onChange={e => handleFilterChange('categoryName', e.target.value)}
                  data-cy="categorySelect"
                >
                  <option value="">All Categories</option>
                  {categories.map(category => (
                    <option key={category.id} value={category.name}>
                      {category.name}
                    </option>
                  ))}
                </Input>
              </FormGroup>

              {/* Rating Filter */}
              <FormGroup>
                <Label className="fw-bold">Minimum Rating</Label>
                <Input
                  type="select"
                  value={filters.minRating}
                  onChange={e => handleFilterChange('minRating', e.target.value)}
                  data-cy="ratingSelect"
                >
                  <option value="">Any Rating</option>
                  <option value="1">1+ Stars</option>
                  <option value="2">2+ Stars</option>
                  <option value="3">3+ Stars</option>
                  <option value="4">4+ Stars</option>
                  <option value="5">5 Stars Only</option>
                </Input>
              </FormGroup>

              {/* Filter Buttons */}
              <div className="d-grid gap-2">
                <Button color="primary" onClick={applyFilters} disabled={loading} data-cy="applyFiltersButton">
                  {loading ? (
                    <>
                      <Spinner size="sm" className="me-2" />
                      Filtering...
                    </>
                  ) : (
                    <>
                      <FontAwesomeIcon icon={faSearch} className="me-2" />
                      Apply Filters
                    </>
                  )}
                </Button>
                <Button color="outline-secondary" onClick={clearFilters} disabled={loading} data-cy="clearFiltersButton">
                  <FontAwesomeIcon icon={faTimesCircle} className="me-2" />
                  Clear All
                </Button>
              </div>
            </CardBody>
          </Card>
        </Col>

        {/* Product Grid */}
        <Col lg={9} md={8}>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="mb-0" data-cy="ProductHeading">
              Products
              <Badge color="secondary" className="ms-2" data-cy="productCount">
                {products.length} found
              </Badge>
            </h2>
            <Button color="primary" tag={Link} to="/product/new" data-cy="entityCreateButton" className="ms-2">
              <FontAwesomeIcon icon={faPlus} />
              &nbsp; Create a new Product
            </Button>
          </div>

          {loading ? (
            <div className="text-center py-5">
              <Spinner color="primary" style={{ width: '3rem', height: '3rem' }} />
              <div className="mt-3">Loading products...</div>
            </div>
          ) : noResults ? (
            <Alert color="warning" data-cy="noProductsAlert">
              <FontAwesomeIcon icon={faTimesCircle} className="me-2" />
              No products found matching your filters. Try adjusting your search criteria.
            </Alert>
          ) : products.length > 0 ? (
            <div data-cy="entityTable">
              <Row>
                {products.map(product => (
                  <Col key={product.id} xl={4} lg={6} md={12} className="mb-4">
                    <Card className="h-100 shadow-sm product-card">
                      <div className="position-relative">
                        {product.imageUrl ? (
                          <img
                            src={product.imageUrl}
                            alt={product.name}
                            className="card-img-top"
                            style={{ height: '200px', objectFit: 'cover' }}
                          />
                        ) : (
                          <div
                            className="card-img-top bg-light d-flex align-items-center justify-content-center"
                            style={{ height: '200px' }}
                          >
                            <span className="text-muted">No Image</span>
                          </div>
                        )}
                        <Badge color="primary" className="position-absolute top-0 end-0 m-2">
                          {product.category?.name}
                        </Badge>
                      </div>
                      <CardBody className="d-flex flex-column">
                        <h5 className="card-title" data-cy="productName">
                          <Link
                            to={`/product/${product.id}`}
                            className="text-decoration-none"
                            onClick={() => trackProductView(product.name)}
                          >
                            {product.name}
                          </Link>
                        </h5>
                        <p className="card-text text-muted small flex-grow-1">{product.description}</p>
                        <div className="mt-auto">
                          <div className="d-flex justify-content-between align-items-center mb-2">
                            <div className="d-flex align-items-center">
                              {renderStars(product.rating)}
                              <span className="ms-2 text-muted">({product.rating != null ? product.rating.toFixed(1) : 'N/A'})</span>
                            </div>
                            <div className="btn-group">
                              <Button
                                size="sm"
                                color="outline-info"
                                onClick={() => trackProductView(product.name)}
                                title="View details"
                                data-cy="entityDetailsButton"
                                tag={Link}
                                to={`/product/${product.id}`}
                              >
                                <FontAwesomeIcon icon={faEye} />
                              </Button>
                              <Button
                                size="sm"
                                color="outline-primary"
                                title="Edit"
                                data-cy="entityEditButton"
                                tag={Link}
                                to={`/product/${product.id}/edit`}
                              >
                                <FontAwesomeIcon icon={faPencilAlt} />
                              </Button>
                              <Button
                                size="sm"
                                color="outline-danger"
                                title="Delete"
                                data-cy="entityDeleteButton"
                                tag={Link}
                                to={`/product/${product.id}/delete`}
                              >
                                <FontAwesomeIcon icon={faTrash} />
                              </Button>
                            </div>
                          </div>
                          <div className="d-flex justify-content-between align-items-center">
                            <h4 className="text-primary mb-0" data-cy="productPrice">
                              ${product.price.toFixed(2)}
                            </h4>
                            <small className="text-muted">by {product.user?.login}</small>
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  </Col>
                ))}
              </Row>
            </div>
          ) : null}
        </Col>
      </Row>
    </Container>
  );
};
