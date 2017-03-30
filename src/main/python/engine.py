# coding=utf-8
import imp
import json
import sys

import numpy
import os
import pandas


def generate_base_rate(_account):
    # 每日基准收益率 = 当日股票池中所有股票的涨（跌）幅直接加总
    # 黑人问号？？？
    base_rate = 0
    for stk in _account.universe:
        base_rate += (_account.close_price[stk] - _account.ref_price[stk]) / _account.ref_price[stk]
    return base_rate


# 补充说明（一）说这里用基准收益率，可是基准收益率是一组数据，不能同无风险利率rf直接相减。我总觉得E(Rm)的意思是基准收益率的期望。。。
def alpha(_base_earning_rate, _annualized_earning_rate, _beta, _risk_free_interest_rate=0.0175):
    return (_annualized_earning_rate - _risk_free_interest_rate) \
           - _beta * numpy.floor(numpy.mean(_base_earning_rate) - _risk_free_interest_rate)


# 同理，补充说明（一）说这里用策略收益率，但是策略收益率为一组数据。我只能理解成年化后的策略收益率（知乎上也是这么写的）
# 附知乎：https://www.zhihu.com/question/27264526
def sharp(_daily_earnings_rate, _annualized_earning_rate, _risk_free_interest_rate=0.0175):
    return (_annualized_earning_rate - _risk_free_interest_rate) / numpy.std(_daily_earnings_rate)


def beta(_daily_earnings_rate, _base_earning_rate):
    n = len(_base_earning_rate)
    if n == 1:
        return 0
    cov = 0
    mean_strategy = numpy.mean(_daily_earnings_rate)
    mean_base = numpy.mean(_base_earning_rate)
    for j in range(0, n):
        cov += (_daily_earnings_rate[j] - mean_strategy) * (_base_earning_rate[j] - mean_base)
    cov = 1.0 / (n - 1) * cov
    return cov / numpy.var(_base_earning_rate)


def get_column_index(key):
    indices = ['', 'date', 'open', 'high', 'low', 'close', 'volume', 'adjclose', 'code', 'name', 'market']
    try:
        return indices.index(key)
    except:
        return -1


def get_universe(prefix):
    times = 6 - len(prefix)
    begin = int(prefix) * (10 ** times)
    end = (int(prefix) + 1) * (10 ** times)
    s = stock_data[get_column_index('code')]
    return s[s.isin(range(begin, end))].drop_duplicates()


class Account:
    def __init__(self, universe, capital):
        self.universe = universe
        self.cash = capital
        self.portfolio = capital
        self.date_index = 0
        self.stocks = {}
        self.sec_pos = {}
        self.ref_price = {}
        self.close_price = {}

    def set_date_index(self, date_index):
        self.date_index = date_index
        code_index = get_column_index('code')
        self.stocks = stock_data[stock_data[code_index].isin(self.universe)
                                 & (stock_data[get_column_index('date')] == trade_days[self.date_index])]
        for index, _info in self.stocks.iterrows():
            self.ref_price[_info[code_index]] = _info[get_column_index('open')]  # 今日开盘价
            self.close_price[_info[code_index]] = _info[get_column_index('close')]  # 今日收盘价
        for stk in self.sec_pos.keys():
            if self.sec_pos[stk] <= 0:
                del self.sec_pos[stk]

    def get_history(self, attr, days):
        column = get_column_index(attr)
        if column == -1:
            return None
        code_column = get_column_index('code')
        result = {}
        for index, _info in self.stocks.iterrows():
            if stock_data.loc[index + days - 1][code_column] == _info[code_column]:
                result[_info[code_column]] = stock_data[column][index:index + days].tolist()
        return result

    def trade(self, stock, target):
        if target < 0 or not self.ref_price[stock]:
            return
        if stock in self.sec_pos:
            curr_amount = self.sec_pos[stock]
        else:
            curr_amount = 0
        diff_amount = target - curr_amount  # 大于0买入，小于0卖出
        new_cash = self.cash - diff_amount * self.ref_price[stock]
        if new_cash < 0:  # 拒绝交易
            return
        self.cash = new_cash
        self.sec_pos[stock] = target

    def sell_all(self):
        for stock in self.sec_pos:
            self.trade(stock, 0)


if __name__ == '__main__':
    args = raw_input()
    args = json.loads(args)
    start_date = args['start_date']
    end_date = args['end_date']  # TODO: auto scale dates
    universe = args['universe']
    frequency = args['frequency']
    strategy = args['strategy']
    capital = 100000000

    os.chdir(os.getcwd())
    stock_data = pandas.read_csv('../../../stock_data.json', sep=',', header=None)
    trade_days = stock_data[1].drop_duplicates()

    # main logic
    start_date_index = trade_days[trade_days == start_date].index[0]
    end_date_index = trade_days[trade_days == end_date].index[0]
    handler = imp.load_source(strategy, 'strategy/' + strategy + '.py')
    account = Account(universe, capital)
    daily_earnings_rate = []
    # 每日基准收益率列表
    daily_base_earnings_rate = []
    for i in range(start_date_index, end_date_index, -frequency):
        account.set_date_index(i)
        handler.handle(account)
        new_portfolio = account.cash
        for stk in account.sec_pos:
            new_portfolio += account.sec_pos[stk] * account.close_price[stk]
        old_portfolio = account.portfolio
        account.portfolio = new_portfolio
        # 每日收益率为(新总额 - 旧总额) ／ 旧总额
        earn_rate = (new_portfolio - old_portfolio) / old_portfolio
        daily_earnings_rate.append(earn_rate)
        daily_base_earnings_rate.append(generate_base_rate(account))

        progress = int((start_date_index - i) * 100.0 / (start_date_index - end_date_index))
        info = {'progress': progress, 'date': trade_days[i], 'cash': account.cash, 'earn_rate': earn_rate}
        print info
        sys.stdout.flush()

    total_earn_rate = (account.portfolio - capital) / capital
    # 这个年化我是乱写的。。。知乎和我的书上的年化方式不一样。。。我也是醉了。。。这里用知乎的
    annualized = total_earn_rate / (start_date_index - end_date_index + 1) * 250  # 这里需要求日期跨度，但是由于早的日期在后面，所以用start - end
    # 总基准收益率 = 每日基准收益率直接平均
    # 再次黑人问号
    # 另外我也不知道这玩意儿要不要年化
    total_base_rate = numpy.mean(daily_base_earnings_rate)
    sharp = sharp(daily_earnings_rate, annualized)
    beta = beta(daily_earnings_rate, daily_base_earnings_rate)
    alpha = alpha(total_base_rate, annualized, beta)

    print json.dumps({'success': True, 'progress': 100, 'daily_earnings_rate': daily_earnings_rate,
                      'annualized_earning_rate': annualized, 'base_earning_rate': daily_base_earnings_rate,
                      'total_base_earning_rate': total_base_rate, 'sharp': sharp, 'beta': beta, 'alpha': alpha})
