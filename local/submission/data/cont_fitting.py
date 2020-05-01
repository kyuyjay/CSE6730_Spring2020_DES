import csv
import numpy as np
import scipy.stats as st

def read_csv(filename):
    header = []
    count = []
    with open(filename, encoding='utf-8-sig') as csvfile:
        csv_read = csv.reader(csvfile)
        for row in csv_read:
            header.append(int(row[0]))
            count.append(int(row[1]))
    return header, count

models = [st.arcsine,st.bradford,st.burr,st.cauchy,st.chi,st.chi2,st.dgamma,st.dweibull,st.expon,st.exponnorm,st.exponweib,st.exponpow,st.fatiguelife,st.fisk,
        st.foldnorm,st.genlogistic,st.gennorm,st.genexpon,
        st.genextreme,st.gausshyper,st.gamma,st.gengamma,st.genhalflogistic,st.gilbrat,st.gompertz,st.gumbel_r,
        st.gumbel_l,st.halflogistic,st.halfnorm,st.halfgennorm,st.hypsecant,st.invgauss,
        st.johnsonsb,st.johnsonsu,st.kstwobign,st.laplace,
        st.logistic,st.loggamma,st.lognorm,st.maxwell,st.nakagami,st.ncx2,
        st.norm,st.pearson3,st.powerlaw,
        st.rayleigh,st.rice,st.recipinvgauss,st.skewnorm,st.t,st.triang,st.truncexpon,
        st.wald,st.weibull_min,st.weibull_max]

header, train = read_csv("training intervals.csv")
print(header)
print(train)


temp, test = read_csv("fitting intervals.csv")
print(test)

data = []
for x in header:
    for fx in range(train[x]):
        data.append(x)

p_chi = []

for dist in models:
    print(dist.name)
    mle = dist.fit(data=data)
    loc = mle[-2]
    scale = mle[-1]
    args = mle[:-2]
    expected = []
    for x in header:
        cdf = dist.cdf(x+0.99,*args,loc=loc,scale=scale) - dist.cdf(x,*args,loc=loc,scale=scale)
        expected.append(cdf * sum(test))
    p_chi.append(st.chisquare(test,expected)[1])
    print(expected)
    expected = []

for dist,p in zip(models,p_chi):
    print("{0:20} {1}".format(dist.name, p))

print("Best model is {} with p value of {}".format(models[np.argmax(p_chi)].name,max(p_chi)))
print(models[np.argmax(p_chi)].fit(data=data));

