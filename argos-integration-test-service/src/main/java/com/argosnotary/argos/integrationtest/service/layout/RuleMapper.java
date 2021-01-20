/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.integrationtest.service.layout;

import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestRule;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = "spring")
public abstract class RuleMapper {

    @Autowired
    private MatchRuleMapper matchRuleMapper;


    @ObjectFactory
    public Rule createRule(RestRule restRule) {
        if (restRule.getRuleType() == RestRule.RuleTypeEnum.MATCH) {
            return matchRuleMapper.mapFromRestRule(restRule);
        }
        return new Rule(RuleType.valueOf(restRule.getRuleType().name()), restRule.getPattern());
    }

    @ObjectFactory
    public RestRule createRule(Rule rule) {
        if (rule instanceof MatchRule) {
            return matchRuleMapper.mapToRestRule((MatchRule) rule);
        }
        return new RestRule();
    }

    public abstract Rule mapFromRestRule(RestRule restRule);

    public abstract RestRule mapFromRestRule(Rule rule);


}

