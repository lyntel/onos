/*
 * Copyright 2014-2016 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.bmv2.api;

import org.onlab.util.KryoNamespace;
import org.onosproject.net.flow.AbstractExtension;
import org.onosproject.net.flow.criteria.ExtensionSelector;
import org.onosproject.net.flow.criteria.ExtensionSelectorType;

public class Bmv2ExtensionSelector extends AbstractExtension implements ExtensionSelector {

    private final KryoNamespace appKryo = new KryoNamespace.Builder().build();
    private Bmv2MatchKey matchKey;

    public Bmv2ExtensionSelector(Bmv2MatchKey matchKey) {
        this.matchKey = matchKey;
    }

    public Bmv2MatchKey matchKey() {
        return matchKey;
    }

    @Override
    public ExtensionSelectorType type() {
        return ExtensionSelectorType.ExtensionSelectorTypes.P4_BMV2_MATCH_KEY.type();
    }

    @Override
    public byte[] serialize() {
        return appKryo.serialize(matchKey);
    }

    @Override
    public void deserialize(byte[] data) {
        matchKey = appKryo.deserialize(data);
    }
}