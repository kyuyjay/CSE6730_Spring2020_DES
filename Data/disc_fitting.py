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

header, train = read_csv("train_12.csv")
print(header)
print(train)
np.ma.masked_equal(train,0)
data = np.divide(train,sum(train))

temp, test = read_csv("test_12.csv")
print(test)

expected = []
for x in header:
    pmf = st.multinomial.pmf(train,1,data)
    expected.append(pmf)
p_chi = st.chisquare(train,pmf * sum(test))
expected = []

print("{0:20} {1}".format("Multinommial",p_chi))



