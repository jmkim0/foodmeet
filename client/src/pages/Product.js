import { useState } from 'react';
import ProductMain from '../components/Product/ProductMain';
import ProductNavbar from '../components/Product/ProductNavbar';
import ProductInfo from '../components/Product/ProductInfo';
import ProductDetail from '../components/Product/ProductDetail';
import ProductReviewList from '../components/Product/ProductReviewList';
import ProductQuestionList from '../components/Product/ProductQuestionList';
import OrderProduct from '../components/Order/OrderProduct';
import OrderModal from '../components/Order/OrderModal';
import {
  ProductWrapper,
  ProductContent,
  ProductOrder,
  ProductButtonContainer,
  ProductModalWrapper,
  ProductModalCancle
} from '../styles/productStyle';
import { OrderButton, DeleteButton } from '../styles/orderStyle';
import { ReactComponent as DeleteIcon } from '../assets/icons/cancleIcon.svg';
import { baseAPI } from '../api/customAxios';
import { useEffect } from 'react';
import Loading from '../components/Layout/Loading';

const Product = () => {
  const productId = decodeURI(window.location.pathname).substring(9);
  const [isOpenOrder, setIsOpenOrder] = useState(false);
  const [count, setCount] = useState(1);
  const [product, setProduct] = useState({});
  const [loading, setLoading] = useState(true);

  const getProduct = async (productId) => {
    try {
      const res = await baseAPI.get(`/product/${productId}`);
      setProduct(res.data.data);
      setLoading(false);
    } catch (err) {
      console.log(err);
    }
  };

  useEffect(() => {
    getProduct(productId);
  }, [productId]);

  useEffect(() => {
    localStorage.removeItem('sort');
  }, []);

  return (
    <>
      {loading ? (
        <Loading />
      ) : (
        <>
          {' '}
          <ProductModalWrapper
            isOpenOrder={isOpenOrder}
            onClick={() => setIsOpenOrder(false)}
          >
            <ProductModalCancle>
              <DeleteButton>
                <DeleteIcon />
              </DeleteButton>
            </ProductModalCancle>
          </ProductModalWrapper>
          <OrderModal
            isOpenOrder={isOpenOrder}
            setIsOpenOrder={setIsOpenOrder}
            count={count}
            setCount={setCount}
            product={product}
          />
          <ProductWrapper>
            <ProductContent>
              <ProductMain product={product} />
              <ProductNavbar product={product} />
              <ProductInfo product={product} />
              <ProductDetail product={product} />
              <ProductReviewList />
              <ProductQuestionList />
            </ProductContent>

            <ProductOrder>
              <OrderProduct
                count={count}
                setCount={setCount}
                product={product}
              />
            </ProductOrder>

            <ProductButtonContainer>
              <OrderButton onClick={() => setIsOpenOrder(true)}>
                구매하기
              </OrderButton>
            </ProductButtonContainer>
          </ProductWrapper>
        </>
      )}
    </>
  );
};

export default Product;
