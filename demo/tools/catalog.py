import json,pathlib
shops=[
('bistro','Bistro Demo','food','Tokoin','#D86B44','Repas chauds',['Riz au poulet','Attieke poisson','Pates tomate','Poulet grille','Brochettes boeuf','Riz aux legumes','Sandwich oeuf','Burger maison','Frites croustillantes','Salade fraiche'],1800,'meal'),
('saveurs','Saveurs Demo','food','Adidogome','#B95054','Cuisine du quartier',['Akoume sauce','Fufu poulet','Riz sauce arachide','Poisson braise','Haricots cuisines','Alloco poulet','Omelette pain','Couscous legumes','Patate douce','Soupe legumes'],1500,'meal'),
('grains','Grains Demo','grocery','Assigame','#B18B3C','Grains et cereales',['Riz blanc','Riz parfume','Haricots rouges','Haricots blancs','Lentilles vertes','Mais sec','Mil dore','Sorgho rouge','Pois chiches','Farine de mais'],950,'kg'),
('marche','Marche Demo','grocery','Be-Kpota','#4D9568','Fruits et legumes',['Tomates','Oignons','Pommes de terre','Carottes','Bananes','Oranges','Pommes','Mangues','Avocats','Aubergines'],1200,'kg'),
('boissons','Boissons Demo','grocery','Ablogame','#388D9C','Boissons et huiles',['Jus de mangue','Jus ananas','Jus orange','Bissap','Gingembre','Huile arachide','Huile tournesol','Sirop citron','Jus tamarin','Eau citronnee'],1600,'l'),
('mode','Mode Demo','fashion','Tokoin','#8C6BB2','Vetements adultes',['T-shirt essentiel','Chemise coton','Robe fluide','Pantalon droit','Polo classique','Veste legere','Jupe longue','Sweat doux','Short quotidien','Blouse elegante'],4500,'clothes'),
('petits','Petits Demo','fashion','Adidogome','#BF759E','Vetements enfants',['T-shirt enfant','Robe enfant','Pantalon enfant','Short enfant','Pyjama enfant','Sweat enfant','Polo enfant','Jupe enfant','Veste enfant','Ensemble enfant'],3000,'kids'),
('pas','Pas Demo','fashion','Hedzranawoe','#657BC0','Chaussures',['Baskets urbaines','Sandales confort','Chaussures ville','Baskets sport','Mocassins souples','Tennis toile','Sandales cuir','Bottines legeres','Claquettes plage','Baskets montantes'],6500,'shoes'),
('tech','Tech Demo','electronics','Hedzranawoe','#4C83A8','Accessoires electroniques',['Chargeur USB-C','Cable tresse','Ecouteurs filaires','Batterie externe','Support telephone','Lampe USB','Souris sans fil','Clavier compact','Hub USB','Enceinte portable'],3500,'unit'),
('maison','Maison Demo','home','Agoe','#BD8554','Maison et cuisine',['Mug ceramique','Assiette creuse','Boite repas','Gourde inox','Torchons coton','Bol bambou','Spatule bois','Plateau repas','Pot rangement','Coussin doux'],2200,'unit'),
('tissus','Tissus Demo','fashion','Assigame','#A36D53','Tissus au metre',['Tissu indigo','Tissu soleil','Tissu foret','Tissu ocean','Tissu corail','Tissu sable','Tissu violet','Tissu ciel','Tissu terracotta','Tissu ivoire'],2800,'m'),
('essentiels','Essentiels Demo','home','Be-Kpota','#62928E','Quotidien et bureau',['Carnet lignes','Stylos couleurs','Trousse zippee','Sac courses','Parapluie compact','Serviette coton','Brosse menage','Panier rangement','Bouteille isotherme','Pochette documents'],1700,'unit')]
result=[]
for si,(slug,name,cat,area,color,kind,names,base,mode) in enumerate(shops):
 products=[]
 for i,n in enumerate(names):
  pid=f'demo-v1-{slug}-{i+1:02}'
  products.append(dict(id=pid,name=n,code=f'{si+1:02}.{i+1:02}',price=base+i*150,mode=mode))
 result.append(dict(id='demo-v1-shop-'+slug,name=name,category=cat,area=area,color=color,kind=kind,products=products))
pathlib.Path('demo/catalog/catalog.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print('Prepared 12 demo shops and 120 unique products.')
