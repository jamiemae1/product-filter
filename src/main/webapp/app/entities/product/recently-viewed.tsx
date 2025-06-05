import React, { useEffect, useState } from 'react';
import { Card, CardBody, CardHeader, Col, Row, Badge, Alert, Spinner } from 'reactstrap';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faClock, faStar, faEye } from '@fortawesome/free-solid-svg-icons';
import axios from 'axios';

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

interface RecentlyViewedProps {
  refreshTrigger?: number;
}

export const RecentlyViewed: React.FC<RecentlyViewedProps> = ({ refreshTrigger = 0 }) => {
  const [recentlyViewedProducts, setRecentlyViewedProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadRecentlyViewedProducts();
  }, [refreshTrigger]);

  const loadRecentlyViewedProducts = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await axios.get('/api/products/recently-viewed');
      setRecentlyViewedProducts(response.data);
    } catch (err) {
      console.error('Error loading recently viewed products:', err);
      setError('Failed to load recently viewed products');
      setRecentlyViewedProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const trackProductView = async (productName: string) => {
    try {
      await axios.post('/api/products/recently-viewed', null, {
        params: { productName },
      });
      // Refresh the list after tracking
      loadRecentlyViewedProducts();
    } catch (err) {
      console.error('Error tracking product view:', err);
    }
  };

  const clearRecentlyViewed = async () => {
    try {
      await axios.delete('/api/products/recently-viewed');
      setRecentlyViewedProducts([]);
    } catch (err) {
      console.error('Error clearing recently viewed products:', err);
      setError('Failed to clear recently viewed products');
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

  if (loading) {
    return (
      <Card className="shadow-sm">
        <CardHeader className="bg-info text-white">
          <FontAwesomeIcon icon={faClock} className="me-2" />
          Recently Viewed Products
        </CardHeader>
        <CardBody className="text-center py-4">
          <Spinner color="primary" />
          <div className="mt-2">Loading recently viewed products...</div>
        </CardBody>
      </Card>
    );
  }

  return (
    <Card className="shadow-sm">
      <CardHeader className="bg-info text-white d-flex justify-content-between align-items-center">
        <div>
          <FontAwesomeIcon icon={faClock} className="me-2" />
          Recently Viewed Products
          {recentlyViewedProducts.length > 0 && (
            <Badge color="light" className="ms-2 text-info">
              {recentlyViewedProducts.length}
            </Badge>
          )}
        </div>
        {recentlyViewedProducts.length > 0 && (
          <button
            className="btn btn-sm btn-outline-light"
            onClick={clearRecentlyViewed}
            title="Clear recently viewed"
            data-cy="clearRecentlyViewedButton"
          >
            Clear
          </button>
        )}
      </CardHeader>
      <CardBody>
        {error && (
          <Alert color="danger" className="mb-3">
            {error}
          </Alert>
        )}

        {recentlyViewedProducts.length === 0 ? (
          <div className="text-center text-muted py-4" data-cy="noRecentlyViewedAlert">
            <FontAwesomeIcon icon={faEye} className="mb-2" size="2x" />
            <p className="mb-0">No recently viewed products</p>
            <small>Products you view will appear here</small>
          </div>
        ) : (
          <div className="recently-viewed-container">
            {/* Horizontal scrollable layout for larger screens */}
            <div className="d-none d-md-block">
              <div className="d-flex overflow-auto pb-2" style={{ gap: '1rem' }}>
                {recentlyViewedProducts.map((product, index) => (
                  <div key={product.id} className="flex-shrink-0" style={{ width: '200px' }} data-cy={`recentlyViewedProduct-${index}`}>
                    <Card className="h-100 shadow-sm recently-viewed-card">
                      <div className="position-relative">
                        {product.imageUrl ? (
                          <img
                            src={product.imageUrl}
                            alt={product.name}
                            className="card-img-top"
                            style={{ height: '120px', objectFit: 'cover' }}
                          />
                        ) : (
                          <div
                            className="card-img-top bg-light d-flex align-items-center justify-content-center"
                            style={{ height: '120px' }}
                          >
                            <span className="text-muted small">No Image</span>
                          </div>
                        )}
                        <Badge color="primary" className="position-absolute top-0 end-0 m-1 small">
                          {product.category?.name}
                        </Badge>
                      </div>
                      <CardBody className="p-2">
                        <h6 className="card-title mb-1" data-cy="recentlyViewedProductName">
                          <Link
                            to={`/product/${product.id}`}
                            className="text-decoration-none"
                            onClick={() => trackProductView(product.name)}
                          >
                            {product.name}
                          </Link>
                        </h6>
                        <div className="d-flex align-items-center mb-1">
                          <div className="small">{renderStars(product.rating)}</div>
                          <span className="ms-1 text-muted small">({product.rating != null ? product.rating.toFixed(1) : 'N/A'})</span>
                        </div>
                        <div className="d-flex justify-content-between align-items-center">
                          <strong className="text-primary" data-cy="recentlyViewedProductPrice">
                            ${product.price.toFixed(2)}
                          </strong>
                        </div>
                      </CardBody>
                    </Card>
                  </div>
                ))}
              </div>
            </div>

            {/* Vertical layout for smaller screens */}
            <div className="d-md-none">
              <Row>
                {recentlyViewedProducts.slice(0, 4).map((product, index) => (
                  <Col xs={6} key={product.id} className="mb-3" data-cy={`recentlyViewedProduct-${index}`}>
                    <Card className="h-100 shadow-sm recently-viewed-card">
                      <div className="position-relative">
                        {product.imageUrl ? (
                          <img
                            src={product.imageUrl}
                            alt={product.name}
                            className="card-img-top"
                            style={{ height: '100px', objectFit: 'cover' }}
                          />
                        ) : (
                          <div
                            className="card-img-top bg-light d-flex align-items-center justify-content-center"
                            style={{ height: '100px' }}
                          >
                            <span className="text-muted small">No Image</span>
                          </div>
                        )}
                      </div>
                      <CardBody className="p-2">
                        <h6 className="card-title mb-1 small" data-cy="recentlyViewedProductName">
                          <Link
                            to={`/product/${product.id}`}
                            className="text-decoration-none"
                            onClick={() => trackProductView(product.name)}
                          >
                            {product.name}
                          </Link>
                        </h6>
                        <div className="small">{renderStars(product.rating)}</div>
                        <strong className="text-primary small" data-cy="recentlyViewedProductPrice">
                          ${product.price.toFixed(2)}
                        </strong>
                      </CardBody>
                    </Card>
                  </Col>
                ))}
              </Row>
              {recentlyViewedProducts.length > 4 && (
                <div className="text-center">
                  <small className="text-muted">and {recentlyViewedProducts.length - 4} more...</small>
                </div>
              )}
            </div>
          </div>
        )}
      </CardBody>
    </Card>
  );
};

// Custom styles for recently viewed cards
const styles = `
.recently-viewed-card {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  cursor: pointer;
}

.recently-viewed-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1) !important;
}

.recently-viewed-container .overflow-auto::-webkit-scrollbar {
  height: 6px;
}

.recently-viewed-container .overflow-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.recently-viewed-container .overflow-auto::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.recently-viewed-container .overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #555;
}
`;

// Inject styles
if (typeof document !== 'undefined') {
  const styleSheet = document.createElement('style');
  styleSheet.type = 'text/css';
  styleSheet.innerText = styles;
  document.head.appendChild(styleSheet);
}

export default RecentlyViewed;
