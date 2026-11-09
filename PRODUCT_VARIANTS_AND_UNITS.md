# Product sizes and measured amounts

Sellers configure these choices in **SequoVendor → Catalog → Edit product → Sizes, amounts & options** after saving the product itself. The customer app displays the saved choices and asks the API to confirm their price before enabling Add.

| Product | Group type | Example choices | Product price means |
| --- | --- | --- | --- |
| Clothing | SIZE | XS, S, M, L, XL | Price of one item; an optional size supplement can be set |
| Shoes | SIZE | EU 38, 39, 40, 41 | Price of one pair; include the sizing system in the group name |
| Colour | COLOR | Blue, black, red | Price of one item plus any selected supplement |
| Rice, grains, beans | MEASURE, unit KG | 0.5, 1, 2, 5 | Price of **1 kg** |
| Oil or another liquid | MEASURE, unit L | 0.5, 1, 2 | Price of **1 litre** |
| Fabric, cable or rope | MEASURE, unit M | 1, 2, 5 | Price of **1 metre** |
| Prepared food | PREPARATION | Spice, side dishes, toppings | Existing base price plus option supplements |

Templates are editable starting points, not verified inventory. Keep only the sizes and amounts actually offered. Set each choice available or unavailable. Shoes can use another sizing system by changing the group name and labels. Other product sizes (small/medium/large, pack dimensions) also use SIZE with seller-defined labels.

SIZE, COLOR and MEASURE require exactly one selection. A product can have several size/colour groups but only one active measurement group. Measured amounts are seller-defined, greater than zero, at most 1000 units, with at most three decimal places. Supported units are KG, G, L, ML, M, CM and UNIT. Conversion between different units is not implicit: the seller's product price always refers to one of the configured units.

For a measured product, the API multiplies the product price by the selected amount using decimal arithmetic, rounds once per portion to the nearest whole CFA (half up), and then applies basket quantity. At 1,000 CFA/kg, three portions of 0.5 kg cost 1,500 CFA. Basket quantity counts portions; it does not change the chosen amount. The chosen amount is printed in the basket and retained in checkout and order snapshots.

The server revalidates option IDs, availability, required choices and prices. Client-supplied totals are not trusted. Changes to choices or prices invalidate the reviewed checkout quote. Existing food options remain compatible. Catalog products gain no invented sizes, weights, prices or availability; sellers must configure them.

Availability here is per choice. This does not introduce a combination-SKU stock ledger (for example stock for red/M versus blue/L); products whose combinations have different availability should be separate seller listings until SKU inventory is implemented.
