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
package com.argosnotary.argos.domain.crypto.signing;

import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * By defining all methods, we force MapStruct to generate new objects for each mapper in stead of
 * taking shortcuts by mapping an object directly.
 */
@Mapper
public interface Cloner {

    Link clone(Link link);

    List<Artifact> cloneArtifacts(List<Artifact> artifacts);

    Artifact clone(Artifact artifact);

    Layout clone(Layout layout);

    List<Step> cloneSteps(List<Step> steps);

    Step clone(Step step);

    List<Rule> clone(List<Rule> rules);

    default Rule clone(Rule rule) {
        if (rule instanceof MatchRule) {
            return clone((MatchRule) rule);
        } else {
            return new Rule(rule.getRuleType(), rule.getPattern());
        }

    }

    MatchRule clone(MatchRule matchRule);

}
