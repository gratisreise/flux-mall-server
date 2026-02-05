# UX Flow - flux-mall

## 1. Member/Auth

### Flow
```
[Login] → [Sign Up] → [Login]
    ↓
[Main Home]
```

### Screen Components

#### Login
- Email input
- Password input
- Login button
- Social login (Google, Kakao, Naver)
- Sign up link

#### Sign Up
- Email input
- Password input
- Password confirmation
- Name input
- Phone number input
- Terms agreement (Agree to all, Terms of Service, Privacy Policy)
- Sign up complete button

---

## 2. Home/Discovery

### Flow
```
[Main Home] → [Category List] → [Product List]
    ↓              ↓
    └──→ [Search Results] → [Product Detail]
```

### Screen Components

#### Main Home
- Search bar
- Category menu
- Banner slider
- Popular products section
- New products section
- Discount products section

#### Category List
- Primary category list
- Secondary category (on selection)

#### Search Results
- Search query
- Sort options (Relevance, Price Low to High, Price High to Low, Newest)
- Filters (Price range, Brand, Delivery type)
- Product list (Thumbnail, Product name, Price, Review count)

---

## 3. Product

### Flow
```
[Product List] → [Product Detail] → [Add to Cart]
      ↓                              ↓
[Product Detail] → [Review Detail]    [Cart]
                    ↓
              [Product Detail] ← Back
```

### Screen Components

#### Product List
- Thumbnail image
- Product name
- Price
- Discount rate (if applicable)
- Review count
- Add to cart button

#### Product Detail
- Product images (Thumbnail + Enlarged)
- Product name
- Price
- Discount rate
- Shipping fee
- Option selection (Size, Color, etc.)
- Quantity selection
- Add to cart button
- Buy now button
- Add to wishlist button
- Product description
- Product review section

#### Review Detail
- Average rating
- Review count
- Review filters (Rating, Photo reviews)
- Review list (Author, Rating, Content, Image, Date)

---

## 4. Cart/Order

### Flow
```
[Cart] → [Order Form] → [Order Complete] → [Order History]
   ↓
[Wishlist] → [Product Detail] → [Cart]
```

### Screen Components

#### Cart
- Product selection checkbox
- Product thumbnail, name, options
- Quantity modification
- Individual price
- Delete button
- Select all / Delete all
- Total payment amount
- Order button

#### Order Form
- Order product information
- Shipping address selection/input (Default address, New address)
- Recipient information
- Delivery request
- Payment method selection
- Product amount, Shipping fee, Discount amount, Total payment amount
- Payment button

#### Order Complete
- Order number
- Order date/time
- Payment amount
- Order product summary
- Order detail button
- Continue shopping button

#### Order History
- Order list (Order number, Date, Product, Status, Amount)
- Order status filter (All, Payment Complete, Shipping, Delivery Complete, Cancel/Return)
- Order detail button

#### Wishlist
- Wishlist product list (Thumbnail, Name, Price)
- Add to cart button
- Delete button

---

## 5. My Page

### Flow
```
[My Page Home]
    ├── [Order/Delivery Inquiry] → [Product Detail]
    ├── [Cancel/Return/Exchange]
    ├── [Wishlist] → [Product Detail]
    ├── [Shipping Address Management]
    └── [Member Info Edit]
```

### Screen Components

#### My Page Home
- Member information summary
- Recent order history
- Orders in progress (Payment Complete, Shipping)
- Quick menu (Order Inquiry, Cancel/Return, Wishlist, Shipping Address)

#### Order/Delivery Inquiry
- Order list
- Order status (Payment Complete, Preparing product, Shipping, Delivery Complete)
- Delivery tracking button
- Cancel/Return application button

#### Cancel/Return/Exchange
- Order history
- Cancel/Return/Exchange eligible products
- Reason selection
- Application button
- Application history inquiry

#### Wishlist
- Wishlist product list (Thumbnail, Name, Price)
- Add to cart button
- Delete button

#### Shipping Address Management
- Shipping address list (Recipient, Address, Contact)
- Default address setting
- Add/Edit/Delete shipping address

#### Member Info Edit
- Name
- Phone number
- Email (Cannot be modified)
- Password change

---

## 6. Seller

### Flow
```
[Seller Home]
    ├── [Product Registration]
    ├── [Product Management] → [Product Edit/Delete]
    ├── [Order Management]
    └── [Sales History Inquiry]
```

### Screen Components

#### Seller Home
- Seller information summary
- Today/This month sales status
- Recent order history
- Quick menu (Product Registration, Product Management, Order Management)

#### Product Registration
- Product name
- Category selection
- Product image upload
- Price
- Discount rate
- Stock quantity
- Options (Size, Color, etc.)
- Product description

#### Product Management
- Product list (Thumbnail, Name, Price, Stock, Approval status)
- Edit button
- Delete button
- Sold out/Sales suspended toggle

#### Order Management
- Order list (Order number, Product, Quantity, Address, Status)
- Start shipping button
- Invoice number input
- Delivery complete processing

#### Sales History Inquiry
- Daily/Monthly sales statistics
- Total sales amount
- Order history detail

---

## 7. Admin

### Flow
```
[Admin Home]
    ├── [Member Management]
    ├── [Seller Management]
    ├── [Product Approval/Management]
    ├── [Order/Delivery Overall Management]
    └── [Category Management]
```

### Screen Components

#### Admin Home (Dashboard)
- Total member/seller/product/order count
- Today's sign-up/sales status
- Pending approval product count
- Latest order/member sign-up history

#### Member Management
- Member list (Name, Email, Sign-up date, Status)
- Search/Filter
- Force withdrawal
- Status change (Active/Inactive)

#### Seller Management
- Seller list (Business name, Representative, Contact, Approval status)
- Approve/Reject
- Seller detail information

#### Product Approval/Management
- Pending approval product list
- Product detail
- Approve/Reject
- Overall product management (Edit, Delete, Force delete)

#### Order/Delivery Overall Management
- Overall order list
- Order status check by order
- Order cancellation processing

#### Category Management
- Primary/Secondary category list
- Add/Edit/Delete category

---

## Overall Flow Summary

```
Non-member: Main Home → Product Discovery → Login/Sign Up
Member: Main Home → Product Discovery → Cart → Order → My Page
Seller: Seller Home → Product Management → Order Management → Sales History
Admin: Admin Home → Member/Seller/Product/Order Management
```
