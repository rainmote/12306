#!/usr/bin/env python

import sys
import json
from fake_useragent import UserAgent

def gen(num, save):
    ua = UserAgent()
    ret = []
    for i in range(num):
        ret.append(ua.random)

    with open(save, 'w') as f:
        json.dump(ret, f)

if __name__ == '__main__':
    num = 1000
    save = 'user_agent.json'
    if len(sys.argv) == 2:
        num = sys.argv[1]
    if len(sys.argv) == 3:
        save = sys.argv[2]
    gen(num, save)
